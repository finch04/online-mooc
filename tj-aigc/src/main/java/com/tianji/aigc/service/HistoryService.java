package com.tianji.aigc.service;

import com.tianji.aigc.vo.MessageVO;

import java.util.List;

public interface HistoryService {
    List<MessageVO> queryHistoryMessage(String sessionId);
}
