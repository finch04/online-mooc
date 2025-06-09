package com.tianji.aigc.controller;

import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.annotations.NoWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @NoWrapper // 不进行包装，直接返回数据
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }

    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.chatService.stop(sessionId);
    }
}
