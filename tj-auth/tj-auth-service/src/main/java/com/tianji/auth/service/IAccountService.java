package com.tianji.auth.service;

import com.tianji.api.dto.user.LoginFormDTO;

import java.util.Map;

/**
 * <p>
 * 账号表，平台内所有用户的账号、密码信息 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-16
 */
public interface IAccountService{

    String login(LoginFormDTO loginFormDTO, boolean isStaff);

    void logout();

    String refreshToken(String refreshToken);

    String wxLogin(String code,String state);

    Map<String, Object> checkWxLoginStatus(String uuid);

    void saveUuid(String uuid);
}
