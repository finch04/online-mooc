package com.tianji.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ServiceException;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.LoginFormDTO;
import com.tianji.auth.common.constants.JwtConstants;
import com.tianji.auth.service.IAccountService;
import com.tianji.auth.service.ILoginRecordService;
import com.tianji.auth.util.JwtTool;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BooleanUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 账号表，平台内所有用户的账号、密码信息 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {
    private final JwtTool jwtTool;
    private final UserClient userClient;
    private final ILoginRecordService loginRecordService;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;

    @Value("${tj.auth.weixin.appid}")
    private String appid;
    @Value("${tj.auth.weixin.secret}")
    private String secret;
    @Value("${tj.auth.weixin.expire}")
    private Long qrcodeExpire; // 二维码有效期，单位秒

    @Override
    public String login(LoginFormDTO loginDTO, boolean isStaff) {
        // 1.查询并校验用户信息
        LoginUserDTO detail = userClient.queryUserDetail(loginDTO, isStaff);
        if (detail == null) {
            throw new BadRequestException("登录信息有误");
        }

        // 2.基于JWT生成登录token
        // 2.1.设置记住我标记
        detail.setRememberMe(loginDTO.getRememberMe());
        // 2.2.生成token
        String token = generateToken(detail);
        // 3.计入登录信息表
        loginRecordService.loginSuccess(loginDTO.getCellPhone(), detail.getUserId());
        // 4.返回结果
        return token;
    }


    @Override
    public void saveUuid(String uuid) {
        // 将UUID存入Redis，有效期5分钟
        redisTemplate.opsForValue().set("wx_qrcode:" + uuid, "pending", qrcodeExpire, TimeUnit.SECONDS);
    }

    @Override
    public Map<String, Object> checkWxLoginStatus(String uuid) {
        String status = redisTemplate.opsForValue().get("wx_qrcode:" + uuid);
        // 处理空结果
        if (status == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "expired");
            return result;
        }
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        return result;
    }

    @Override
    public String wxLogin(String code, String state) {
        // 检查Redis中是否存在该UUID
        String storedState = redisTemplate.opsForValue().get("wx_qrcode:" + state);
        if (storedState == null) {
            throw new BadRequestException("二维码已过期或UUID无效");
        }

        // 删除Redis中的UUID
        redisTemplate.delete("wx_qrcode:" + state);

        // 1. 使用code换取access_token和openid
        Map<String, String> tokenInfo = getAccess_token(code);
        String accessToken = tokenInfo.get("access_token");
        String openid = tokenInfo.get("openid");

        // 2. 先尝试获取用户基本信息（包含是否绑定手机号标识）
        Map<String, String> userInfo = getUserinfo(accessToken, openid);

        // 3. 解析出微信unionid
        LoginUserDTO loginUser;
        String unionid;
        if (userInfo.containsKey("unionid") && StringUtils.isNotBlank(userInfo.get("unionid"))) {
            // 微信用户已绑定手机号，直接通过手机号查询用户
            unionid = userInfo.get("unionid");
            LoginFormDTO loginForm = new LoginFormDTO();
            //这里先用password暂存一下unionid
            loginForm.setPassword(unionid);
            loginForm.setType(3);

            loginUser = userClient.queryUserDetail(loginForm, false);

            if (loginUser == null) {
                // 用户不存在，需要引导用户注册或绑定账号
                throw new BadRequestException("该微信未绑定用户，请先注册账号");
            }
        } else {
            // 微信用户未绑定手机号，需要引导用户授权获取手机号
            throw new BadRequestException("第三方系统调用异常");
        }

        // 4. 生成token
        String token = generateToken(loginUser);
        // 5. 记录登录信息
        //这里如果要用unionid传入会被提示数据长度太长（因为实际上是手机号的字段）
        loginRecordService.loginSuccess(null, loginUser.getUserId());
        return token;
    }


    private String generateToken(LoginUserDTO detail) {
        // 2.2.生成access-token
        String token = jwtTool.createToken(detail);
        // 2.3.生成refresh-token，将refresh-token的JTI 保存到Redis
        String refreshToken = jwtTool.createRefreshToken(detail);
        // 2.4.将refresh-token写入用户cookie，并设置HttpOnly为true
        int maxAge = BooleanUtils.isTrue(detail.getRememberMe()) ?
                (int) JwtConstants.JWT_REMEMBER_ME_TTL.toSeconds() : -1;
        WebUtils.cookieBuilder()
                .name(detail.getRoleId() == 2 ? JwtConstants.REFRESH_HEADER : JwtConstants.ADMIN_REFRESH_HEADER)
                .value(refreshToken)
                .maxAge(maxAge)
                .httpOnly(true)
                .build();
        return token;
    }

    @Override
    public void logout() {
        // 删除jti
        jwtTool.cleanJtiCache();
        // 删除cookie
        WebUtils.cookieBuilder()
                .name(JwtConstants.REFRESH_HEADER)
                .value("")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 1.校验refresh-token,校验JTI
        LoginUserDTO userDTO = jwtTool.parseRefreshToken(refreshToken);
        // 2.生成新的access-token、refresh-token
        return generateToken(userDTO);
    }

    /**
     * 携带授权码申请令牌
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     *
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * @param code 授权
     * @return
     */
    private Map<String, String> getAccess_token(String code) {
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 最终的请求路径
        String url = String.format(url_template, appid, secret, code);

        // 远程调用此url
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 获取响应的结果
        String result = exchange.getBody();
        // 将result转成map
        return JSON.parseObject(result, Map.class);
    }

    /**
     * 携带令牌查询用户信息
     *
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     *
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     *
     * }
     * @param access_token
     * @param openid
     * @return
     */
    private Map<String, String> getUserinfo(String access_token, String openid) {
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(url_template, access_token, openid);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        // 获取响应的结果
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // 将result转成map
        return JSON.parseObject(result, Map.class);
    }
}