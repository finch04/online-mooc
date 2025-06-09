package com.tianji.chat;/**
 * @author fsq
 * @date 2025/6/7 12:00
 */

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import org.apache.http.client.ResponseHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

/**
 * @Author: fsq
 * @Date: 2025/6/7 12:00
 * @Version: 1.0
 */
@SpringBootTest
public class ChatTest {

    @Test
    public void test() throws Exception {
        OpenAiChatModel chatGlmChatModel = OpenAiChatModel.builder()
                // Ollama 服务地址
                .baseUrl("http://localhost:11434/v1")
                // 模型key
                .apiKey("EMPTY")
                // 最大令牌数
                .maxTokens(1000)
                // 精确度
                .temperature(0d)
                // 超时时间
                .timeout(Duration.ofSeconds(15))
                // 模型名称
                .modelName("deepseek-r1:1.5b")
                // 重试次数
                .maxRetries(3)
                .build();

        String me = "你好!请给我讲一个笑话";
        System.out.println("用户：" + me);
        String content = chatGlmChatModel.generate(me);
        System.out.println("AI：" + content);

    }

    @Test
    public static void main(String[] args) {
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("http://localhost:11434/v1")
                // 模型key
                .apiKey("EMPTY")
                // 最大令牌数
                .maxTokens(1000)
                // 精确度
                .temperature(0d)
                // 超时时间
                .timeout(Duration.ofSeconds(15))
                // 模型名称
                .modelName("deepseek-r1:1.5b")
                .build();


        String userMessage = "你好，给我讲个笑话吧~";

        model.generate(userMessage, new StreamingResponseHandler() {
            @Override
            public void onNext(String s) {
                System.out.println("onPartialResponse: " + s);
            }

            @Override
            public void onComplete(Response response) {
                System.out.println("onCompleteResponse: " + response);
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
    }


    @Test
    public void test2() {
//        Assistant assistant = AiServices.builder(Assistant.class)
//                .chatLanguageModel(model)
//                .tools(new Tools())
//                .build();
//
//        String answer = assistant.chat("What is 1+2 and 3*4?");
    }
}
