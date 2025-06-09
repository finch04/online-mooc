package com.tianji.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.config.AiConfig;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.query.RecordQuery;
import com.tianji.chat.mapper.ChatSessionMapper;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * <p>
 * 聊天对话的每个片段记录（分片存储） 服务实现类
 * </p>
 *
 * @author lusy
 * @since 2025-05-06
 */
@Service
@Slf4j
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

    private final AiConfig.AssistantRedis assistantRedis;
    private final AiConfig.KnowledgeAdvisor knowledgeAdvisor;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final ChatLanguageModel chatLanguageModel;

    @Autowired
    public ChatSessionServiceImpl(@Lazy AiConfig.AssistantRedis assistantRedis,
                                  @Lazy AiConfig.KnowledgeAdvisor knowledgeAdvisor,
                                  @Lazy EmbeddingStore<TextSegment> embeddingStore,
                                  StreamingChatLanguageModel streamingChatLanguageModel,
                                  ChatLanguageModel chatLanguageModel) {
        this.assistantRedis = assistantRedis;
        this.knowledgeAdvisor = knowledgeAdvisor;
        this.embeddingStore = embeddingStore;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.chatLanguageModel=chatLanguageModel;
    }


    @Override
    public String chat(String sessionId, String message) {
         assistantRedis.chat(sessionId, message);

        return chatLanguageModel.generate(message);
//        TokenStream advise = knowledgeAdvisor.advise(sessionId, message);
    }

    @Override
    public Flux<String> stream(String sessionId, String message) {
        if(UserContext.getUser()==null){
            return Flux.error(new RuntimeException("请先登录"));
        }
        assistantRedis.chat(sessionId, message);

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        streamingChatLanguageModel.generate(message, new dev.langchain4j.model.StreamingResponseHandler() {
            @Override
            public void onNext(String s) {
                log.info("{}",s);
                // 检查特殊字符
                if("\n".equals(s)) {
                    System.out.println("收到换行符");
                } else if(s.contains(" ")) {
                    System.out.println("收到包含空格的内容: " + s);
                }
                // 格式化并发送 SSE 消息
                sink.tryEmitNext(formatSseMessage(s));
            }

            @Override
            public void onComplete(Response response) {
                sink.tryEmitNext(formatSseMessage("[DONE]"));
                sink.tryEmitComplete();
                log.info("数据接收完成！");
            }

            @Override
            public void onError(Throwable error) {
                sink.tryEmitError(error);
            }
        });

        return sink.asFlux();
    }

    private String formatSseMessage(String data) {
        // 处理换行符
        data =data.replace(" ", "&nbsp;");
        return data.replace("\n", "\ndata: ") + "\n\n";
    }

    @Override
    public PageDTO<ChatSession> getRecord(RecordQuery query) {
        Page<ChatSession> page = this.lambdaQuery()
                .eq(ChatSession::getSessionId,query.getSessionId())
                .eq(ChatSession::getUserId,UserContext.getUser()).page(query.toMpPageDefaultSortByCreateTimeDesc());
        return PageDTO.of(page);
    }
}
