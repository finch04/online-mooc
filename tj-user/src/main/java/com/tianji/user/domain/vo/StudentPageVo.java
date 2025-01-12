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
@Schema(description = "学生信息")
public class StudentPageVo {
    @Schema(description = "学生id，也是用户id", example = "1")
    private Long id;

    @Schema(description = "学生名称", example = "张三")
    private String name;

    @Schema(description = "头像", example = "default-icon.jpg")
    private String icon;

    @Schema(description = "手机号", example = "13800010004")
    private String cellPhone;

    @Schema(description = "性别：0-男性，1-女性", example = "0")
    private Integer gender;

    @Schema(description = "购买/报名课程数量", example = "12")
    private Integer courseAmount;

    @Schema(description = "注册时间", example = "2022-07-12")
    private LocalDateTime createTime;

    @Schema(description = "账户状态，0-禁用，1-正常", example = "1")
    private Integer status;
}
