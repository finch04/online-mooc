package com.tianji.aigc.controller;

import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.aigc.vo.TemplateVO;
import com.tianji.common.annotations.NoWrapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private static final TemplateVO TEMPLATE_VO = new TemplateVO();

    @NoWrapper // 自定义注解，记过不进行包装
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }

    // @NoWrapper // 自定义注解，记过不进行包装
    // @PostMapping(value = "/mock",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public Flux<ChatEventVO> chatMock(@RequestBody ChatDTO chatDTO) {
    //     return this.chatService.chatMock2(chatDTO.getQuestion(), chatDTO.getSessionId());
    // }

    @PostMapping("/text")
    public String chatText(@RequestBody String question) {
        return this.chatService.chatText(question);
    }


    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.chatService.stop(sessionId);
    }

    @GetMapping("/templates")
    public TemplateVO getTemplates() {
        return TEMPLATE_VO;
    }
}
