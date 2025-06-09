package com.tianji.chat.config;

import com.tianji.chat.service.ToolsService;
import com.tianji.common.utils.SPELUtils;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class AiConfig {
    public interface AssistantRedis {
        // 阻塞式
        String chat(@MemoryId String memoryId, @UserMessage String message);
        // 流式响应
        TokenStream stream(@MemoryId String memoryId, @UserMessage String message);
        //获取历史记录
        List<ChatMessage> getHistory(@MemoryId String memoryId);
    }

    // ----------------------------- 存储到 Redis -----------------------------
    @Autowired
    private PersistentChatMemoryStore store;

    @Autowired
    private ToolsService toolsService;

    @Bean
    public AssistantRedis assistantRedis(ChatLanguageModel qwenChatModel,
                                         StreamingChatLanguageModel qwenStreamingChatModel) {
        return AiServices.builder(AssistantRedis.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
//                .tools(toolsService)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .maxMessages(500)
                                .id(memoryId)
                                .chatMemoryStore(store)
                                .build()
                )
                .build();
    }

    // ------------------------------ 构建提示词 StructuredPrompt ---------------------------------------
    public interface KnowledgeAdvisor {
        @SystemMessage("你是一位智能学习助手，帮助学生根据他们上传的学习资料回答问题。 请根据学生提供的知识内容，用清晰、准确、简洁的语言回答，尽量避免使用模糊或复杂的术语。 如果提问不在知识库范围内，你可以利用你已知的知识进行回答,并在最后附上：'这个问题好像不在你的笔记中，建议你查阅更多资料或补充相关内容。")
        TokenStream advise(@MemoryId String memoryId, @UserMessage String question);
    }

    @Bean
    public KnowledgeAdvisor knowledgeAdvisor(ChatLanguageModel qwenChatModel,
                                             StreamingChatLanguageModel qwenStreamingChatModel) {
        return AiServices.builder(KnowledgeAdvisor.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .tools(toolsService)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .maxMessages(500)
                                .id(memoryId)
                                .chatMemoryStore(store)
                                .build()
                )
                .build();
    }
}