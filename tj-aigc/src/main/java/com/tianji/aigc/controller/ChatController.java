package com.tianji.aigc.controller;

import com.tianji.aigc.service.ChatService;
import com.tianji.common.annotations.NoWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class ChatController {

    @Resource
    private ChatService chatService;

    @NoWrapper // 自定义注解，记过不进行包装
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam("q") String question, @RequestParam("sessionId") String sessionId) {
        return this.chatService.chat(question, sessionId);
    }

    @PostMapping("/chat/text")
    public String chatText(@RequestParam("q") String question) {
        return this.chatService.chatText(question);
    }


    @PostMapping("/chat/stop")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.chatService.stop(sessionId);
    }
}
