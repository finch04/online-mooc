package com.tianji.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.config.AiConfig;
import com.tianji.chat.config.PersistentChatMemoryStore;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.query.RecordQuery;
import com.tianji.chat.mapper.ChatSessionMapper;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.OnCompleteOrOnError;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.tianji.chat.constants.RedisConstants.DELAY_TASK_EXECUTE_TIME;
import static dev.langchain4j.data.message.ChatMessageSerializer.messageToJson;

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
//@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {


    private final AiConfig.AssistantRedis assistantRedis;
    private final AiConfig.KnowledgeAdvisor knowledgeAdvisor;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public ChatSessionServiceImpl(@Lazy AiConfig.AssistantRedis assistantRedis,
                                  @Lazy AiConfig.KnowledgeAdvisor knowledgeAdvisor,
                                  @Lazy EmbeddingStore<TextSegment> embeddingStore,
                                  StreamingChatLanguageModel streamingChatLanguageModel,
                                  ChatLanguageModel chatLanguageModel,
                                  StringRedisTemplate redisTemplate) {
        this.assistantRedis = assistantRedis;
        this.knowledgeAdvisor = knowledgeAdvisor;
        this.embeddingStore = embeddingStore;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.chatLanguageModel = chatLanguageModel;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public String chat(String sessionId, String message) {
        return  assistantRedis.chat(sessionId, message);
    }

    @Override
    public SseEmitter stream(String sessionId, String message) {
        if (UserContext.getUser() == null) {
            // 创建一个立即错误的SseEmitter
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new RuntimeException("请先登录"));
            return emitter;
        }

        // 创建SseEmitter，设置超时时间为30分钟
        SseEmitter emitter = new SseEmitter(1800000L);

        // 添加超时和完成回调
        emitter.onTimeout(emitter::complete);
        emitter.onCompletion(() -> log.info("SSE流已完成"));
        emitter.onError(error -> log.error("SSE流发生错误", error));

        StringBuilder responseBuilder = new StringBuilder();
        StringBuilder originBuilder = new StringBuilder();

        try {
            // 调用生成方法
            streamingChatLanguageModel.generate(message, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String s) {
                    try {
                        // 格式化并发送SSE消息
                        String sse = formatSseMessage(s);
                        originBuilder.append(sse);
                        log.info("{}", s);

                        // 检查特殊字符
                        if ("\n".equals(s)) {
                            System.out.println("收到换行符");
                        } else if (s.contains(" ")) {
                            System.out.println("收到包含空格的内容: " + s);
                        }

                        responseBuilder.append(s);

                        // 通过SseEmitter发送消息
                        emitter.send(SseEmitter.event()
                                .data(sse, MediaType.TEXT_PLAIN)
                                .name("message"));
                    } catch (IOException e) {
                        log.error("发送SSE消息失败", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(Response response) {
                    try {
                        // 发送完成消息
                        emitter.send(SseEmitter.event()
                                .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                                .name("message"));

                        // 完成SseEmitter
                        emitter.complete();
                        log.info("数据接收完成！\n{}", responseBuilder.toString());
                        log.info("纯发送的消息：\n{}", originBuilder.toString());
                    } catch (IOException e) {
                        log.error("发送完成消息失败", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("生成过程发生错误", error);
                    emitter.completeWithError(error);
                }
            });
        } catch (Exception e) {
            log.error("生成过程发生异常", e);
            emitter.completeWithError(e);
        }

        assistantRedis.chat(sessionId, message);
        return emitter;
    }

    //    @Override
//    public Flux<String> stream(String sessionId, String message) {
//        if(UserContext.getUser()==null){
//            return Flux.error(new RuntimeException("请先登录"));
//        }
//        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
//        StringBuilder responseBuilder = new StringBuilder();
//        StringBuilder originBuilder = new StringBuilder();
//        streamingChatLanguageModel.generate(message,new StreamingResponseHandler<AiMessage>() {
//            @Override
//            public void onNext(String s) {
//                // 格式化并发送 SSE 消息
//                String sse = formatSseMessage(s);
//                sink.tryEmitNext(sse);
//                originBuilder.append(sse);
//                log.info("{}", s);
//                // 检查特殊字符
//                if ("\n".equals(s)) {
//                    System.out.println("收到换行符");
//                } else if (s.contains(" ")) {
//                    System.out.println("收到包含空格的内容: " + s);
//                }
//                responseBuilder.append(s);
//            }
//
//            @Override
//            public void onComplete(Response response) {
//                sink.tryEmitNext(formatSseMessage("[DONE]"));
//                sink.tryEmitComplete();
//                log.info("数据接收完成！\n{}",responseBuilder.toString());
//                log.info("纯发送的消息：\n{}", originBuilder.toString());
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                sink.tryEmitError(error);
//            }
//        });
//
//        assistantRedis.chat(sessionId, message);
//        return sink.asFlux();
//    }
    private String formatSseMessage(String data) {
        data = data.replace(" ", "&nbsp;"); // 替换空格为 HTML 实体
        return  data;  // 符合 SSE 协议格式
    }

/**
 * TODO 这里有问题，必须调用AssistantRedis才能实现AI对话的完整存储，但是调用后本身chat方法就交给他了。
 * TODO 但是这个方法在当前版本有冲突，无法使用流式调用，导致如果使用流式调用就无法自动存储。
 * TODO 现在就导致了有可能一开始AI给你响应的对话结果和最终服务器存储历史记录的答案不一致。
 * TODO 相关文档：https://docs.langchain4j.info/tutorials/ai-services#%E6%B5%81%E5%BC%8F%E5%A4%84%E7%90%86
 * TODO 一个比较好的解决方案就是在Service实现类中将持久化中的操作redis方法复刻一遍，但是我觉得会降低可读性、以及耦合性太严重，打算后期查阅一些资料看是否有解决方案
 */


//    @Override
//    public Flux<String> stream(String sessionId, String message) {
//        if (UserContext.getUser() == null) {
//            return Flux.error(new RuntimeException("请先登录"));
//        }
//
//        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
//
//        // 获取原始 Flux
//        TokenStream tokenStreamFlux = assistantRedis.stream(sessionId, message);
//        // 注册回调处理流式数据
//        tokenStreamFlux.onNext(s -> {
//            handleData(s, sink);
//        }).onComplete(i -> {
//            handleComplete(sink);
//        }).onError(throwable -> {
//            sink.tryEmitError(throwable);
//        });
//
//        // 返回处理后的 Flux
//        return sink.asFlux();
//    }

//    private void handleData(String s, Sinks.Many<String> sink) {
//        log.info("Flux 数据: {}", s);
//        // 特殊字符处理
//        if ("\n".equals(s)) {
//            System.out.println("收到换行符");
//        } else if (s.contains(" ")) {
//            System.out.println("收到包含空格的内容: " + s);
//        }
//        // 格式化并发送
//        String formattedData = formatSseMessage(s);
//        sink.tryEmitNext(formattedData);
//    }
//
//    private void handleComplete(Sinks.Many<String> sink) {
//        log.info("Flux 完成，发送 [DONE]");
//        String doneMessage = formatSseMessage("[DONE]");
//        sink.tryEmitNext(doneMessage); // 发送完成消息
//        sink.tryEmitComplete(); // 结束流
//    }







    @Override
    public PageDTO<ChatSession> getRecord(RecordQuery query) {
        Page<ChatSession> page = this.lambdaQuery()
                .eq(ChatSession::getSessionId,query.getSessionId())
                .eq(ChatSession::getUserId,UserContext.getUser()).page(query.toMpPageDefaultSortByCreateTimeDesc());
        return PageDTO.of(page);
    }


}
