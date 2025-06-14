package com.tianji.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.domain.dto.PromptBuilder;
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
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.tianji.chat.config.AiConfig;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

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
@RequiredArgsConstructor
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
    public PageDTO<ChatSession> getRecord(RecordQuery query) {
        Page<ChatSession> page = this.lambdaQuery()
                .eq(ChatSession::getSessionId,query.getSessionId())
                .eq(ChatSession::getUserId,UserContext.getUser()).page(query.toMpPageDefaultSortByCreateTimeDesc());
        return PageDTO.of(page);
    }

    private String formatSseMessage(String data) {
        data = data.replace(" ", "&nbsp;"); // 替换空格为 HTML 实体
        return  data;  // 符合 SSE 协议格式
    }


    //测试模式不生成历史记录，使用原来调用大模型方法实现
    @Override
    public SseEmitter test(String sessionId, String message) {
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
//        assistantRedis.chat(sessionId, message);
        return emitter;
    }

    @Override
    public SseEmitter stream(String memoryId, String message) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new RuntimeException("请先登录"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(1800000L);
        StringBuilder responseBuilder = new StringBuilder();
        StringBuilder originBuilder = new StringBuilder();
        AtomicBoolean isStreamCompleted = new AtomicBoolean(false); // 标记流状态

        // 统一回调设置
        emitter.onTimeout(() -> {
            log.warn("SSE流超时，但无法终止TokenStream（接口不支持）");
            try {
                emitter.send(SseEmitter.event()
                        .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                        .name("message"));
            } catch (IOException e) {
                log.error("发送超时完成消息失败", e);
            }
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            if (!isStreamCompleted.get()) {
                log.warn("SSE流被客户端主动关闭，但TokenStream可能仍在运行");
            }
        });

        emitter.onError(error -> {
            log.error("SSE流发生错误", error);
            try {
                emitter.send(SseEmitter.event()
                        .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                        .name("message"));
            } catch (IOException e) {
                log.error("发送错误完成消息失败", e);
            }
            emitter.complete();
        });

        try {
            TokenStream stream = assistantRedis.stream(memoryId, message);

            stream.onNext(token -> {
                try {
                    String sse = formatSseMessage(token);
                    originBuilder.append(sse);
                    responseBuilder.append(token);
                    emitter.send(SseEmitter.event()
                            .data(sse, MediaType.TEXT_PLAIN)
                            .name("message"));
                } catch (IOException e) {
                    log.error("发送SSE消息失败", e);
                    try {
                        emitter.send(SseEmitter.event()
                                .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                                .name("message"));
                    } catch (IOException ex) {
                        log.error("发送失败完成消息失败", ex);
                    }
                    emitter.completeWithError(e);
                }
            }).onComplete(s -> {
                isStreamCompleted.set(true);
                try {
                    emitter.send(SseEmitter.event()
                            .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                            .name("message"));
                    emitter.complete();
                    log.info("数据接收完成：{}", responseBuilder.toString());
                } catch (IOException e) {
                    log.error("发送完成消息失败", e);
                }
            }).onError(error -> {
                isStreamCompleted.set(true);
                log.error("生成过程发生错误", error);
                try {
                    emitter.send(SseEmitter.event()
                            .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                            .name("message"));
                } catch (IOException e) {
                    log.error("发送错误完成消息失败", e);
                }
                emitter.complete();
            }).start(); // 启动流

        } catch (Exception e) {
            log.error("初始化TokenStream失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                        .name("message"));
            } catch (IOException ex) {
                log.error("发送初始化失败完成消息失败", ex);
            }
            emitter.completeWithError(e);
        }
        return emitter;
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
        StringBuilder originBuilder = new StringBuilder();
        AtomicBoolean isStreamCompleted = new AtomicBoolean(false); // 标记流状态

        // 添加超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("文件SSE流超时，但无法终止TokenStream（接口不支持）");
            try {
                emitter.send(SseEmitter.event()
                        .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                        .name("message"));
            } catch (IOException e) {
                log.error("发送超时完成消息失败", e);
            }
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            if (!isStreamCompleted.get()) {
                log.warn("文件SSE流被客户端主动关闭，但TokenStream可能仍在运行");
            }
        });

        emitter.onError(error -> {
            log.error("文件SSE流发生错误", error);
            try {
                emitter.send(SseEmitter.event()
                        .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                        .name("message"));
            } catch (IOException e) {
                log.error("发送错误完成消息失败", e);
            }
            emitter.complete();
        });

        Long userId = UserContext.getUser();
        try {
            // 1. 向量化问题
            Embedding queryEmbedding = embeddingModel.embed(message).content();

            // 2. 查询向量数据库
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

            // 构造系统消息，使用 Markdown 格式渲染知识库资料
//            String systemMessageContent = String.format(
//                            "以下是与问题相关的参考资料：\n" +
//                            "> \n" +
//                            "%s\n" +
//                            "> \n" +
//                            "\n" +"%s\n",
//                    context,message
//            );
            String systemMessageContent = PromptBuilder.buildSystemMessage(context, message);

            // 使用 TokenStream 接收流
            TokenStream stream = knowledgeAdvisor.advise(sessionId, systemMessageContent,systemMessageContent);

            stream.onNext(token -> {
                try {
                    String sse = formatSseMessage(token);
                    originBuilder.append(sse);
                    // 检查特殊字符
                    if ("\n".equals(token)) {
                        // System.out.println("收到换行符");
                    } else if (token.contains(" ")) {
                        // System.out.println("收到包含空格的内容: " + token);
                    }
                    // 通过SseEmitter发送消息
                    emitter.send(SseEmitter.event()
                            .data(sse, MediaType.TEXT_PLAIN)
                            .name("message"));
                } catch (IOException e) {
                    log.error("发送SSE消息失败", e);
                    try {
                        emitter.send(SseEmitter.event()
                                .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                                .name("message"));
                    } catch (IOException ex) {
                        log.error("发送失败完成消息失败", ex);
                    }
                    emitter.completeWithError(e);
                }
            }).onComplete(s -> {
                isStreamCompleted.set(true);
                try {
                    emitter.send(SseEmitter.event()
                            .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                            .name("message"));
                    emitter.complete();
                     log.info("纯发送的消息：\n{}", originBuilder.toString());
                } catch (IOException e) {
                    log.error("发送完成消息失败", e);
                }
            }).onError(error -> {
                isStreamCompleted.set(true);
                log.error("生成过程发生错误", error);
                try {
                    emitter.send(SseEmitter.event()
                            .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                            .name("message"));
                } catch (IOException e) {
                    log.error("发送错误完成消息失败", e);
                }
                emitter.complete();
            }).start(); // 启动流

        } catch (Exception e) {
            log.error("生成过程发生异常", e);
            try {
                emitter.send(SseEmitter.event()
                        .data(formatSseMessage("[DONE]"), MediaType.TEXT_PLAIN)
                        .name("message"));
            } catch (IOException ex) {
                log.error("发送初始化失败完成消息失败", ex);
            }
            emitter.completeWithError(e);
        }
        return emitter;
    }



}
