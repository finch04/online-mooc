package com.tianji.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.tianji.auth.constants.AuthConstants;
import com.tianji.auth.service.ICodeService;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.RandomUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.message.api.client.AsyncSmsClient;
import com.tianji.message.api.client.MessageClient;
import com.tianji.message.domain.dto.SmsInfoDTO;
import com.tianji.message.domain.enums.SmsTemplate;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.tianji.api.constants.SmsConstants.VERIFY_CODE_PARAM_NAME;
import static com.tianji.common.constants.ErrorInfo.Msg.INVALID_VERIFY_CODE;


/**
 * <p>
 * 账户、角色关联表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodeServiceImpl  implements ICodeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AsyncSmsClient asyncSmsClient;
//    private final RocketMqHelper rocketMqHelper;

    @Override
    public void sendVerifyCode(String phone) {
        String key = AuthConstants.USER_VERIFY_CODE_KEY + phone;
        // 1.查看code是否存在
        String code = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(code)){
            // 2.生成随机验证码
            code = RandomUtils.randomNumbers(4);
            // 3.保存到redis
            stringRedisTemplate.opsForValue()
                    .set(AuthConstants.USER_VERIFY_CODE_KEY + phone, code, AuthConstants.USER_VERIFY_CODE_TTL);
        }
        // 4.发送短信
        log.debug("发送短信验证码：{}", code);
        SmsInfoDTO info = new SmsInfoDTO();
        info.setPhones(CollUtils.singletonList(phone));
        info.setTemplateCode(SmsTemplate.VERIFY_CODE.toString());
        Map<String, String> params = new HashMap<>(1);
        params.put(VERIFY_CODE_PARAM_NAME, code);
        info.setTemplateParams(params);
        asyncSmsClient.sendMessage(info);

//        rocketMqHelper.sendSync(MqConstants.Topic.MESSAGE_TOPIC, info);

    }

    @Override
    public void verifyCode(String phone, String code) {
        String cacheCode = stringRedisTemplate.opsForValue().get(AuthConstants.USER_VERIFY_CODE_KEY + phone);
        if (!StringUtils.equals(cacheCode, code)) {
            // 验证码错误
            throw new BadRequestException(INVALID_VERIFY_CODE);
        }
        stringRedisTemplate.delete(AuthConstants.USER_VERIFY_CODE_KEY + phone);
    }
}
