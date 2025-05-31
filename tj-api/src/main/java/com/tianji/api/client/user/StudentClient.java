package com.tianji.api.client.user;


import com.tianji.api.client.user.fallback.UserClientFallback;
import com.tianji.api.dto.user.LoginFormDTO;
import com.tianji.api.dto.user.StudentFormDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.LoginUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "student",value = "user-service", fallbackFactory = UserClientFallback.class)
public interface StudentClient {

    /**
     * 学员注册接口
     * @return 用户详情
     */
    @PostMapping("/students/register")
    void registerStudent(@RequestBody StudentFormDTO studentFormDTO);


    /**
     * 根据手机号找回密码
     */
    @PutMapping("/students/password")
    void updateMyPassword(@RequestBody StudentFormDTO studentFormDTO);
}
