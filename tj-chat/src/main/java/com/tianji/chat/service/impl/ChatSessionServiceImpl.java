package com.tianji.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.config.AiConfig;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.query.RecordQuery;
import com.tianji.chat.mapper.ChatSessionMapper;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.chat.utils.QdrantEmbeddingUtils;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

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
    private final EmbeddingModel embeddingModel;
    private final QdrantClient qdrantClient;

    @Autowired
    public ChatSessionServiceImpl(@Lazy AiConfig.AssistantRedis assistantRedis,
                                  @Lazy AiConfig.KnowledgeAdvisor knowledgeAdvisor,
                                  @Lazy EmbeddingStore<TextSegment> embeddingStore,
                                  StreamingChatLanguageModel streamingChatLanguageModel,
                                  ChatLanguageModel chatLanguageModel,
                                  StringRedisTemplate redisTemplate, EmbeddingModel embeddingModel, QdrantClient qdrantClient) {
        this.assistantRedis = assistantRedis;
        this.knowledgeAdvisor = knowledgeAdvisor;
        this.embeddingStore = embeddingStore;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.chatLanguageModel = chatLanguageModel;
        this.redisTemplate = redisTemplate;
        this.embeddingModel = embeddingModel;
        this.qdrantClient = qdrantClient;
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

    @Override
    public SseEmitter fileStream(String sessionId, String message) {
        // 检查用户是否登录
        if (UserContext.getUser() == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new RuntimeException("请先登录"));
            return emitter;
        }

        // 创建SseEmitter，设置超时时间为30分钟
        SseEmitter emitter = new SseEmitter(1800000L);

        // 添加超时和完成回调
        emitter.onTimeout(emitter::complete);
        emitter.onCompletion(() -> log.info("文件SSE流已完成"));
        emitter.onError(error -> log.error("文件SSE流发生错误", error));

        StringBuilder originBuilder = new StringBuilder();
        Long userId = UserContext.getUser();
        try {
            // 1. 向量化问题
            Embedding queryEmbedding = embeddingModel.embed(message).content();

            // 2. 查询向量数据库  在这个版本似乎没用
//            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
//                    .queryEmbedding(queryEmbedding)
//                    .filter(metadataKey("user_id").isEqualTo(userId))
//                    .maxResults(3)
//                    .minScore(0.7)
//                    .build();
//
//            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
            //彻底解决了搜索结果
            Points.Filter filter = Points.Filter.newBuilder().addMust(matchKeyword("user_id", userId.toString())).build();
            List<Points.ScoredPoint> results = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                    .setCollectionName(QDRANT_COLLECTION)
                    .addAllVector(queryEmbedding.vectorAsList())
                    .setLimit(3)
                    .setWithPayload(enable(true))
                    .setWithVectors(WithVectorsSelectorFactory.enable(true))
                    .setFilter(filter)
                    .build()).get();

            List<EmbeddingMatch<TextSegment>> matches = results.stream()
                    .map(point -> QdrantEmbeddingUtils.toEmbeddingMatch(point, queryEmbedding, "text_segment"))
                    .collect(Collectors.toList());
//            Collections.reverse(matches);


            // 打印匹配结果用于调试
            for (EmbeddingMatch<TextSegment> match : matches) {
                log.info("匹配得分: {}", match.score());
                log.info("匹配内容:\n{}", match.embedded().text());
            }

            // 3. 拼接 context 参考材料
            String context = matches.stream()
                    .map(match -> "- " + match.embedded().text())
                    .collect(Collectors.joining("\n"));

            // 4. 构造一个增强版问题（加入 context）
            String enhancedQuestion = String.format(
                    "以下是一些参考资料：\n" +
                            "%s\n" +
                            "\n" +
                            "请根据上面的资料回答这个问题：%s\n" +
                            "回答时请注意以下几点：\n" +
                            "1. 不要提到信息来源或对背景知识做任何评价。\n" +
                            "2. 背景内容可能包含与问题无关的信息，你需要自行判断哪些内容是有用的。\n" +
                            "3. 如果完全没有相关信息，可以根据常识推断并在最后加一句：‘回答来源于网络，请您自行甄别哦~’",
                    context, message
            );

            streamingChatLanguageModel.generate(enhancedQuestion, new StreamingResponseHandler<AiMessage>() {
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


            // 5. 调用LLM接口获取流式回答
//            TokenStream advise = knowledgeAdvisor.advise(sessionId, enhancedQuestion);
//
//            // 6. 设置令牌流的处理逻辑
//            advise.onNext(token -> {
//                try {
//                    // 格式化并发送SSE消息
//                    String sse = formatSseMessage(token);
//                    originBuilder.append(sse);
//                    log.debug("收到令牌: {}", token);
//
//                    responseBuilder.append(token);
//
//                    // 通过SseEmitter发送消息
//                    emitter.send(SseEmitter.event()
//                            .data(sse, MediaType.TEXT_PLAIN)
//                            .name("message"));
//                } catch (IOException e) {
//                    log.error("发送SSE消息失败", e);
//                    emitter.completeWithError(e);
//                }
//            }).onComplete(s -> {
//                try {
//                    // 发送完成消息
//                    emitter.send(SseEmitter.event()
//                            .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
//                            .name("message"));
//
//                    // 完成SseEmitter
//                    emitter.complete();
//                    log.info("文件问答数据接收完成！\n{}", responseBuilder.toString());
//                    log.info("纯发送的消息：\n{}", originBuilder.toString());
//                } catch (IOException e) {
//                    log.error("发送完成消息失败", e);
//                    emitter.completeWithError(e);
//                }
//            }).onError(error -> {
//                log.error("生成过程发生错误", error);
//                emitter.completeWithError(error);
//            });
//        } catch (Exception e) {
//            log.error("文件问答过程发生异常", e);
//            emitter.completeWithError(e);
//        }

        // 记录聊天历史
//        assistantRedis.chat(sessionId, message);
        return emitter;
    }



}
