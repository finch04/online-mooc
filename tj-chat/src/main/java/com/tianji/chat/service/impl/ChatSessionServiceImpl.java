package com.tianji.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.config.AiConfig;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.mapper.ChatSessionMapper;
import com.tianji.chat.service.IChatSessionService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
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

    @Autowired
    public ChatSessionServiceImpl(@Lazy AiConfig.AssistantRedis assistantRedis,
                                  @Lazy AiConfig.KnowledgeAdvisor knowledgeAdvisor,
                                  @Lazy EmbeddingStore<TextSegment> embeddingStore,
                                  StreamingChatLanguageModel streamingChatLanguageModel) {
        this.assistantRedis = assistantRedis;
        this.knowledgeAdvisor = knowledgeAdvisor;
        this.embeddingStore = embeddingStore;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
    }


    @Override
    public String chat(String memoryId, String message) {
        return assistantRedis.chat(memoryId, message);

    }

    @Override
    public Flux<String> stream(String memoryId, String message) {

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        streamingChatLanguageModel.generate(message, new dev.langchain4j.model.StreamingResponseHandler() {
            @Override
            public void onNext(String s) {
                for (int i = 0; i < s.length(); i++) {
                    sink.tryEmitNext(String.valueOf(s.charAt(i)));
                }
            }

            @Override
            public void onComplete(Response response) { // 在完成时发送 [DONE] 事件
                sink.tryEmitNext("[DONE]");
                sink.tryEmitComplete();
            }

            @Override
            public void onError(Throwable error) {
                sink.tryEmitError(error);
            }
        });

        return sink.asFlux();
    }

//    @Override
//    public Flux<String> FileStream(String memoryId, String message) {
//        // 1. 向量化问题
//        Embedding queryEmbedding = embeddingModel.embed(message).content();
//
//        // 2. 查询向量数据库
//        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
//                .queryEmbedding(queryEmbedding)
//                .filter(metadataKey("userId").isEqualTo(37L))
//                .maxResults(3)
//                .minScore(0.7)
//                .build();
//
//        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
//
//        for (EmbeddingMatch<TextSegment> match : matches) {
//            System.out.println("匹配得分: " + match.score());
//            System.out.println("匹配内容:\n" + match.embedded().text());
//        }
//
//        // 3. 拼接 context 参考材料
//        String context = matches.stream()
//                .map(match -> "- " + match.embedded().text())
//                .collect(Collectors.joining("\n"));
//
//        // 4. 构造一个增强版问题（加入 context）
//       String enhancedQuestion = String.format(
//            "以下是一些参考资料：\n" +
//            "%s\n" +
//            "\n" +
//            "请根据上面的资料回答这个问题：%s\n" +
//            "回答时请注意以下几点：\n" +
//            "1. 不要提到信息来源或对背景知识做任何评价。\n" +
//            "2. 背景内容可能包含与问题无关的信息，你需要自行判断哪些内容是有用的。\n" +
//            "3. 如果完全没有相关信息，可以根据常识推断并在最后加一句：‘回答来源于网络，请您自行甄别哦~’",
//            context, message
//       );
//
//
//        // 5. 调用你已经实现的 LLM 接口
//        TokenStream advise = knowledgeAdvisor.advise(memoryId, enhancedQuestion);
//
//        return Flux.create(fluxSink -> {
//            advise.onPartialResponse(fluxSink::next)
//                    .onCompleteResponse(chatResponse -> fluxSink.complete())
//                    .onError(fluxSink::error)
//                    .start();
//        });
//    }
}
