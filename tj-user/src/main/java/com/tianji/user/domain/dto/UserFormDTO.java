package com.tianji.user.domain.dto;

import com.tianji.api.dto.user.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "修改用户信息的表单，带有密码")
public class UserFormDTO extends UserDTO {
    @Schema(description = "原始密码", example = "123321")
    @NotNull
    private String oldPassword;

    @Schema(description = "新密码", example = "123321")
    @NotNull
    private String password;
}
