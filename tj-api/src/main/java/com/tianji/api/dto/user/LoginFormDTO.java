package com.tianji.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "登录表单实体")
public class LoginFormDTO {
    @Schema(description = "登录方式：1-密码登录; 2-验证码登录", example = "1", required = true)
    @NotNull
    private Integer type;
    @Schema(description = "用户名", example = "jack")
    private String username;
    @Schema(description = "手机号", example = "13800010001")
    private String cellPhone;
    @Schema(description = "密码", example = "123", required = true)
    @NotNull
    private String password;
    @Schema(description = "7天免密登录", example = "true")
    private Boolean rememberMe = false;
}
