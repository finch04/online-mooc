package com.tianji.chat.config;

import com.tianji.chat.config.PersistentChatMemoryStore;
import com.tianji.chat.service.ToolsService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AiConfig {
    public interface AssistantRedis {
        // 阻塞式
        String chat(@MemoryId String memoryId, @UserMessage String message);

        // 流式响应
        @SystemMessage ("你叫小天，是天机学堂的智能学习助手。专注为学生解答问题，回答要简洁明了，语气亲切。若问题超出知识范围，就说‘这个问题我暂时还不清楚，你可以问问老师或查阅资料哦～’")
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


    // ------------------------------ 构建提示词 StructuredPrompt ---------------------------------------
    public interface KnowledgeAdvisor {
//        @SystemMessage("你是一位智能学习助手，帮助学生根据他们上传的学习资料回答问题。 请根据学生提供的知识内容，用清晰、准确、简洁的语言回答，尽量避免使用模糊或复杂的术语。 如果提问不在知识库范围内，你可以利用你已知的知识进行回答,并在最后附上：'这个问题好像不在你的笔记中，建议你查阅更多资料或补充相关内容。{{answerInstructions}}")
        @SystemMessage("你是一位智能学习助手，核心职责是基于学生上传的学习资料精准回答问题。请严格遵循以下规则：\n" +
                "资料处理规则\n" +
                "格式识别：学生提供的参考资料会以 Markdown 格式呈现，且被固定标记包裹：\n" +
                "[SYS_CONTEXT_BEGIN]（参考资料起始）\n" +
                "[SYS_CONTEXT_END]（参考资料结束）\n" +
                "注意：标记后直接跟随学生提问，无需处理代码标记。\n" +
                "内容处理：\n" +
                "仅参考[SYS_CONTEXT_BEGIN]与[SYS_CONTEXT_END]之间的资料内容，不解释代码逻辑；\n" +
                "回答需使用清晰、简洁的语言，避免专业术语堆砌；\n" +
                "若提问超出资料范围，可基于常识回答，但需在结尾附加提示：\n" +
                "\"这个问题好像不在你的笔记中，建议补充相关资料或查阅更多学习资源哦～\"\n" +
                "回答规范\n" +
                "准确性：优先依据资料内容，确保信息正确；\n" +
                "简洁性：直接针对问题核心，避免冗余扩展；\n" +
                "友好性：语气亲和，适合学生理解（例如用 \"可以试试...\" 替代 \"建议采取...\"）。\n" +
                "示例响应逻辑：\n" +
                "若资料包含 \"光合作用原理\"，学生提问 \"植物如何制造氧气？\"，则回答：\n" +
                "植物通过光合作用制造氧气。叶绿体利用光能将二氧化碳和水转化为有机物，同时释放氧气。\n" +
                "请始终以学生提供的资料为首要参考依据，在保持专业性的同时确保回答易于理解。")
        TokenStream advise(@MemoryId String memoryId, @UserMessage String question,@V("answerInstructions") String systemMessageContent);
    }

    @Bean
    public KnowledgeAdvisor knowledgeAdvisor(StreamingChatLanguageModel qwenStreamingChatModel) {
        return AiServices.builder(KnowledgeAdvisor.class)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .tools(toolsService)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .maxMessages(1000)
                                .id(memoryId)
                                .chatMemoryStore(store)
                                .build()
                )
                .build();
    }
}