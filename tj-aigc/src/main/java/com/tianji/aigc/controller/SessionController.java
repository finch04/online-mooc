package com.tianji.aigc.controller;

import com.tianji.aigc.domain.vo.AgentConfigVO;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.domain.vo.ChatSessionVO;
import com.tianji.aigc.domain.vo.MessageVO;
import com.tianji.aigc.domain.vo.SessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Tag(name = "会话管理接口")
public class SessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 新建会话
     */
    @Operation(summary = "新建会话")
    @PostMapping
    public SessionVO createSession(@RequestParam(value = "n", defaultValue = "3") Integer num) {
        return this.chatSessionService.createSession(num);
    }

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    @Operation(summary = "获取热门会话")
    @GetMapping("/hot")
    public List<AgentConfigVO.Example> hotExamples(@RequestParam(value = "n", defaultValue = "3") Integer num) {
        return this.chatSessionService.hotExamples(num);
    }

    /**
     * 查询单个历史对话详情
     *
     * @return 对话记录列表
     */
    @Operation(summary = "查询单个历史对话详情")
    @GetMapping("/{sessionId}")
    public List<MessageVO> queryBySessionId(@PathVariable("sessionId") String sessionId) {
        return this.chatSessionService.queryBySessionId(sessionId);
    }

    /**
     * 查询历史会话列表
     */
    @Operation(summary = "查询历史会话列表")
    @GetMapping("/history")
    public Map<String, List<ChatSessionVO>> queryHistorySession() {
        return this.chatSessionService.queryHistorySession();
    }

    /**
     * 删除历史会话列表
     */
    @Operation(summary = "删除历史会话列表")
    @DeleteMapping("/history")
    public void deleteHistorySession(@RequestParam("sessionId") String sessionId) {
        this.chatSessionService.deleteHistorySession(sessionId);
    }

    /**
     * 更新历史会话标题
     */
    @Operation(summary = "更新历史会话标题")
    @PutMapping("/history")
    public void updateTitle(@RequestParam("sessionId") String sessionId,
                            @RequestParam("title") String title) {
        this.chatSessionService.updateTitle(sessionId, title);
    }
}
