package com.tianji.chat.controller;

import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.query.RecordQuery;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.common.annotations.NoWrapper;
import com.tianji.common.domain.dto.PageDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

    @NoWrapper // 不进行包装，直接返回数据  这里可以通过原本Flux返回数据（通过自定义非包装注解使得不会返回data）
    @ApiOperation("流式聊天")
    @GetMapping(value = "/",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter memoryChatRedisStream(@RequestParam(defaultValue = "我叫finch，你叫什么名字？") String message,
                                            @RequestParam(defaultValue = "1") String sessionId) {
        return chatSessionService.stream(sessionId, message);
    }

    @ApiOperation("根据知识库内容流式聊天")
    @GetMapping(value = "/file",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter memoryChatFileStream(@RequestParam(defaultValue = "我叫finch，你叫什么名字？") String message,
                                            @RequestParam(defaultValue = "1") String sessionId) {
        return chatSessionService.fileStream(sessionId, message);
    }

    @ApiOperation("测试流式聊天接口")
    @GetMapping(value = "/test",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter test(@RequestParam(defaultValue = "我叫finch，你叫什么名字？") String message,
                                           @RequestParam(defaultValue = "1") String sessionId) {
        return chatSessionService.test(sessionId, message);
    }

    @ApiOperation("获取聊天记录")
    @GetMapping("/records")
    public PageDTO<ChatSession> getRecord(RecordQuery query) {
        return chatSessionService.getRecord(query);
    }

}