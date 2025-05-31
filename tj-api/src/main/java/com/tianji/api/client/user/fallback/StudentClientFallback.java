package com.tianji.api.client.user.fallback;

import com.tianji.api.client.user.StudentClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.LoginFormDTO;
import com.tianji.api.dto.user.StudentFormDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.LoginUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;

@Slf4j
public class StudentClientFallback implements FallbackFactory<StudentClient> {
    @Override
    public StudentClient create(Throwable cause) {
        log.error("查询用户服务出现异常", cause);
        return new StudentClient() {
            @Override
            public void registerStudent(StudentFormDTO studentFormDTO) {

            }

            @Override
            public void updateMyPassword(StudentFormDTO studentFormDTO) {

            }
        };
    }
}
