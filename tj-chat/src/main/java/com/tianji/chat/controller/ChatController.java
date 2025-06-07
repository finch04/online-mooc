package com.tianji.chat.controller;

import com.tianji.chat.service.IChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatController {


    private final IChatSessionService chatSessionService;

    @GetMapping("/assistant/redis")
    public String memoryChatRedis(@RequestParam(defaultValue = "我叫finch") String message,
                                  @RequestParam(defaultValue = "1") String memoryId) {
        System.out.println("message = " + message);
        System.out.println("memoryId = " + memoryId);

        return chatSessionService.chat(memoryId, message);
    }

    @GetMapping(value = "/assistant/redis/stream",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> memoryChatRedisStream(@RequestParam(defaultValue = "我叫finch") String message,
                                              @RequestParam(defaultValue = "1") String memoryId) {
            return chatSessionService.stream(memoryId, message);
    }

//    @GetMapping(value = "file/stream", produces = "text/stream;charset=UTF-8")
//    public Flux<String> FileChatStream(@RequestParam(defaultValue = "我叫finch") String message,
//                                              @RequestParam(defaultValue = "1") String memoryId) {
//
//        return chatSessionService.FileStream(memoryId, message);
//    }

}