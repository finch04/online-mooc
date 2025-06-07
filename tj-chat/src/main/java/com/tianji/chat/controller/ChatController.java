package com.tianji.chat.controller;

import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.service.IChatSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
@Api(tags = "聊天接口")
public class ChatController {


    private final IChatSessionService chatSessionService;

    @ApiOperation("普通聊天，非流式")
    @GetMapping("/simple")
    public String memoryChatRedis(@RequestParam(defaultValue = "我叫finch，你叫什么名字？") String message,
                                  @RequestParam(defaultValue = "1") String sessionId) {
        System.out.println("message = " + message);
        System.out.println("memoryId = " + sessionId);

        return chatSessionService.chat(sessionId, message);
    }

    @ApiOperation("流式聊天")
    @GetMapping(value = "/",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> memoryChatRedisStream(@RequestParam(defaultValue = "我叫finch，你叫什么名字？") String message,
                                              @RequestParam(defaultValue = "1") String sessionId) {
        return chatSessionService.stream(sessionId, message);
    }

    @ApiOperation("获取聊天记录")
    @GetMapping("/{id}")
    public List<ChatSession> getRecord(@PathVariable("id") String sessionId) {
        return chatSessionService.getRecord(sessionId);
    }

}