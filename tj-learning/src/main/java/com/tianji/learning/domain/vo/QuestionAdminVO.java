package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户端互动问题信息")
public class QuestionAdminVO {
    @Schema(description = "主键id")
    private Long id;
    @Schema(description = "互动问题名称")
    private String title;
    @Schema(description = "互动问题描述")
    private String description;
    @Schema(description = "回答数量，0表示没有回答")
    private Integer answerTimes;
    @Schema(description = "创建时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
    @Schema(description = "管理端问题状态：0-未查看，1-已查看")
    private Integer status;
    @Schema(description = "是否被隐藏")
    private Boolean hidden;
    @Schema(description = "提问者昵称")
    private String userName;
    @Schema(description = "提问者头像")
    private String userIcon;
    @Schema(description = "课程名称")
    private String courseName;
    @Schema(description = "教师名称")
    private String teacherName;
    @Schema(description = "章名称")
    private String chapterName;
    @Schema(description = "节名称")
    private String sectionName;
    @Schema(description = "三级分类名称，中间使用/隔开")
    private String categoryName;
}
