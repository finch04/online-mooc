package com.tianji.aigc.controller;

import com.tianji.aigc.service.HistoryService;
import com.tianji.aigc.vo.MessageVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoryController {

    @Resource
    private HistoryService historyService;

    /**
     * 查询历史对话记录
     *
     * @return 对话记录列表
     */
    @GetMapping("/history")
    public List<MessageVO> queryHistoryMessage(@RequestParam("sessionId") String sessionId) {
        return this.historyService.queryHistoryMessage(sessionId);
    }
}
