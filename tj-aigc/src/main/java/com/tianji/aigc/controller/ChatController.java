package com.tianji.aigc.controller;

import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.domain.dto.ChatDTO;
import com.tianji.aigc.domain.vo.ChatEventVO;
import com.tianji.aigc.domain.vo.TemplateVO;
import com.tianji.common.annotations.NoWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat")
@Tag(name = "智能助手接口")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private static final TemplateVO TEMPLATE_VO = new TemplateVO();

    @NoWrapper // 标记结果不进行包装
    @Operation(summary = "智能助手问答接口")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }

    @Operation(summary = "停止当前会话")
    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.chatService.stop(sessionId);
    }

    @Operation(summary = "根据文本回答（非流式调用）")
    @PostMapping("/text")
    public String chatText(@RequestBody String question) {
        return this.chatService.chatText(question);
    }

    @Operation(summary = "获取智能助手模板")
    @GetMapping("/templates")
    public TemplateVO getTemplates() {
        return TEMPLATE_VO;
    }
}
