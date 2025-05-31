package com.tianji.auth.service.impl;/**
 * @author fsq
 * @date 2025/5/20 13:41
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.api.client.user.StudentClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.StudentFormDTO;
import com.tianji.auth.service.ICodeService;
import com.tianji.auth.service.IUserService;
import com.tianji.common.exceptions.BizIllegalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: fsq
 * @Date: 2025/5/20 13:41
 * @Version: 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl  implements IUserService {

    private final ICodeService codeService;
    private final StudentClient studentClient;
    private final UserClient userClient;

    @Override
    public void register(StudentFormDTO dto) {
        codeService.verifyCode(dto.getCellPhone(), dto.getCode());
        studentClient.registerStudent(dto);
    }

    @Override
    public void updateMyPassword(StudentFormDTO dto) {
        Long id = userClient.exchangeUserIdWithPhone(dto.getCellPhone());
        if(id==null){
            throw new BizIllegalException("手机号未注册");
        }
//        codeService.verifyCode(dto.getCellPhone(), dto.getCode());
        studentClient.updateMyPassword(dto);
    }
}
