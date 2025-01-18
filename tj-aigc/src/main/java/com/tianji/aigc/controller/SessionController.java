package com.tianji.aigc.controller;

import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.aigc.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 新建会话
     */
    @PostMapping
    public SessionVO createSession() {
        return this.chatSessionService.createSession();
    }

    /**
     * 第一次对话时，需要保存标题数据
     */
    @PutMapping
    public void updateTitle(@RequestParam("sessionId") String sessionId,
                            @RequestParam("title") String title) {
        this.chatSessionService.updateTitle(sessionId, title);
    }

    /**
     * 查询历史会话列表
     */
    @GetMapping("/history")
    public List<ChatSessionVO> queryHistorySession() {
        return this.chatSessionService.queryHistorySession();
    }
}
