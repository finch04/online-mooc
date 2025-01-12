package com.tianji.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工用户")
public class StaffVO {
    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "头像", example = "default-user-icon.jpg")
    private String icon;

    @Schema(description = "手机号", example = "13800010002")
    private String cellPhone;

    @Schema(description = "员工姓名", example = "user_138foo0002")
    private String name;

    @Schema(description = "角色id", example = "5")
    private Long roleId;

    @Schema(description = "角色名称", example = "5")
    private String roleName;

    @Schema(description = "注册时间", example = "2022-07-22")
    private LocalDateTime createTime;

    @Schema(description = "账号状态", example = "0")
    private Integer status;
}
