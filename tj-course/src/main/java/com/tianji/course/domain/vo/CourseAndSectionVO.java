package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 课程和目录及学习进度信息
 */
@Data
@Schema(description = "课程和目录及学习进度信息")
public class CourseAndSectionVO {
    @Schema(description = "课程id", example = "1")
    private Long id;

    @Schema(description = "课程名称", example = "Java编程基础")
    private String name;

    @Schema(description = "课程封面", example = "http://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "课程章节数量", example = "5")
    private Integer sections;

    @Schema(description = "教师头像", example = "http://example.com/teacher.jpg")
    private String teacherIcon;

    @Schema(description = "教师名称", example = "张三")
    private String teacherName;

    @Schema(description = "课程id", example = "1")
    private Long lessonId;

    @Schema(description = "正在学习的小节id", example = "101")
    private Long latestSectionId;

    @Schema(description = "章列表")
    private List<ChapterVO> chapters;
}
