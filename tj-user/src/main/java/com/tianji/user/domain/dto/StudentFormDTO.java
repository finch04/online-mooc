package com.tianji.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生注册和修改密码的表单实体")
public class StudentFormDTO {

    @Schema(description = "手机号", example = "13800010004")
    private String cellPhone;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "验证码", example = "645632")
    private String code;
}
