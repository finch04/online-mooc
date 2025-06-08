package com.tianji.chat.controller;

import com.tianji.chat.domain.dto.UserSessionDTO;
import com.tianji.chat.domain.po.UserSession;
import com.tianji.chat.service.IUserSessionService;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
@Slf4j
@Api(tags = "用户会话关联接口")
public class UserSessionController {

    private final IUserSessionService userSessionService;

    @ApiOperation("创建用户会话关联")
    @PostMapping
    public void createUserSession(@RequestBody UserSessionDTO dto) {
        dto.setUserId(UserContext.getUser());
        userSessionService.createUserSession(dto);
    }

    @ApiOperation("修改用户会话关联")
    @PutMapping("/{id}")
    public void updateUserSession(@PathVariable("id") Long id,
                                  UserSessionDTO dto) {
        dto.setUserId(UserContext.getUser());
        userSessionService.updateUserSession(id,dto);
    }

    @ApiOperation("查询用户会话列表")
    @GetMapping("/list")
    public List<UserSession> getUserSessionList() {
        return userSessionService.getUserSessionList();
    }


    @ApiOperation("根据ID删除用户会话关联")
    @DeleteMapping("/{id}")
    public void deleteUserSession(@PathVariable Long id) {
        userSessionService.deleteUserSession(id);
    }
}