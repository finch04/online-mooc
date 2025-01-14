package com.tianji.aigc.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.tianji.aigc.config.SessionProperties;
import com.tianji.aigc.vo.SessionVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    @Resource
    private SessionProperties sessionProperties;

    @PostMapping("/session")
    public SessionVO createSession() {
        SessionVO sessionVO = BeanUtil.toBean(sessionProperties, SessionVO.class);
        // 随机生成sessionId
        sessionVO.setSessionId(IdUtil.fastSimpleUUID());
        return sessionVO;
    }

}
