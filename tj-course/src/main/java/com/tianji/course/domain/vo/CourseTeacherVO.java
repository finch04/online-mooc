package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 老师课程信息
 */
@Data
@Schema(description = "老师课程信息")
public class CourseTeacherVO {
    @Schema(description = "老师课程关系id", example = "1")
    private Long id;

    @Schema(description = "老师头像", example = "http://example.com/icon.jpg")
    private String icon;

    @Schema(description = "形象照", example = "http://example.com/photo.jpg")
    private String photo;

    @Schema(description = "老师姓名", example = "张三")
    private String name;

    @Schema(description = "老师介绍", example = "资深Java开发工程师")
    private String introduce;

    @Schema(description = "用户端是否显示", example = "true")
    private Boolean isShow;

    @Schema(description = "职位", example = "讲师")
    private String job;
}
