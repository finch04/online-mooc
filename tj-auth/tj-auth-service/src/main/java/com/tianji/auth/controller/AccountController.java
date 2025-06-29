package com.tianji.auth.controller;


import com.tianji.api.dto.user.LoginFormDTO;
import com.tianji.auth.common.constants.JwtConstants;
import com.tianji.auth.service.IAccountService;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * 账户登录相关接口
 */
@RestController
@RequestMapping("/accounts")
@Api(tags = "账户管理")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    @ApiOperation("登录并获取token")
    @PostMapping(value = "/login")
    public String loginByPw(@RequestBody LoginFormDTO loginFormDTO) {
        return accountService.login(loginFormDTO, false);
    }

    @ApiOperation("保存前端生成的UUID到Redis")
    @PostMapping("/wx/saveUuid")
    public void saveUuid(@RequestBody Map<String, String> data) {
        String uuid = data.get("uuid");
        if (uuid == null) {
            throw new BadRequestException("UUID不能为空");
        }
        accountService.saveUuid(uuid);
    }

    @ApiOperation("检查微信登录状态")
    @GetMapping("/wx/check")
    public Map<String, Object> checkWxLoginStatus(@RequestParam String uuid) {
        Map<String, Object> result = accountService.checkWxLoginStatus(uuid);
        return result;
    }

    @ApiOperation("微信登录并获取token（包含回调）")
    @GetMapping(value = "/wxLogin")
    public String wxLogin(@RequestParam String code, @RequestParam(required = false) String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        return accountService.wxLogin(code, state);
    }


    @ApiOperation("管理端登录并获取token")
    @PostMapping(value = "/admin/login")
    public String adminLoginByPw(@RequestBody LoginFormDTO loginFormDTO) {
        return accountService.login(loginFormDTO, true);
    }

    @ApiOperation("退出登录")
    @PostMapping(value = "/logout")
    public void logout() {
        accountService.logout();
    }

    @ApiOperation("刷新token")
    @GetMapping(value = "/refresh")
    public String refreshToken(
            @CookieValue(value = JwtConstants.REFRESH_HEADER, required = false) String studentToken,
            @CookieValue(value = JwtConstants.ADMIN_REFRESH_HEADER, required = false) String adminToken
    ) {
        if (studentToken == null && adminToken == null) {
            throw new BadRequestException("登录超时");
        }
        String host = WebUtils.getHeader("origin");
        if (host == null) {
            throw new BadRequestException("登录超时");
        }
        String token = host.startsWith("www", 7) ? studentToken : adminToken;
        if (token == null) {
            throw new BadRequestException("登录超时");
        }
        return accountService.refreshToken(WebUtils.cookieBuilder().decode(token));
    }
}
