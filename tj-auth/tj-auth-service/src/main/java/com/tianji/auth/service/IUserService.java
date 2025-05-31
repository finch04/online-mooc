package com.tianji.auth.service;

import com.tianji.api.dto.user.StudentFormDTO;

/**
 * @author fsq
 * @date 2025/5/20 13:41
 */
public interface IUserService {
    /**
     * 注册用户
     * @param dto 注册信息
     * @return 是否注册成功
     */
    void register(StudentFormDTO dto);

    void updateMyPassword(StudentFormDTO dto);
}
