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
@Schema(description = "分页教师信息")
public class TeacherPageVO {
    @Schema(description = "教师id，也是用户id", example = "1")
    private Long id;

    @Schema(description = "教师名称", example = "罗老师")
    private String name;

    @Schema(description = "头像", example = "default-user-icon.jpg")
    private String icon;

    @Schema(description = "手机号", example = "13980019001")
    private String cellPhone;

    @Schema(description = "岗位", example = "讲师")
    private String job;

    @Schema(description = "介绍", example = "黑马高级Java讲师")
    private String intro;

    @Schema(description = "负责的课程数量", example = "10")
    private Integer courseAmount;

    @Schema(description = "出题数量", example = "18")
    private Integer examQuestionAmount;

    @Schema(description = "注册时间", example = "2022-07-12")
    private LocalDateTime createTime;

    @Schema(description = "账户状态，0-禁用，1-正常", example = "1")
    private Integer status;

    @Schema(description = "形象照片地址", example = "default-user-icon.jpg")
    private String photo;
}
