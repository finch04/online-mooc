package com.tianji.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserBasicVO {
    @Schema(description = "用户id", example = "1")
    private Long id;

    @Schema(description = "用户名称/昵称", example = "李四")
    private String name;

    @Schema(description = "用户类型,1-员工,2-普通学员，3-老师", example = "2")
    private Integer type;

    @Schema(description = "头像", example = "default-user-icon.jpg")
    private String icon;
}
