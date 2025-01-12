package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目详情
 */
@Data
@Schema(description = "题目详情")
public class SubjectInfoVO {
    @Schema(description = "题目id", example = "1")
    private Long id;

    @Schema(description = "名称", example = "Java题")
    private String name;

    @Schema(description = "所属题目分类")
    private List<CateSimpleInfoVO> cates;

    @Schema(description = "题目类型", example = "1")
    private Integer subjectType;

    @Schema(description = "题目难易度", example = "1")
    private Integer difficulty;

    @Schema(description = "分值", example = "5")
    private Integer score;

    @Schema(description = "更新时间", example = "2022-07-11T20:54:00")
    private LocalDateTime updateTime;

    @Schema(description = "更新人", example = "张三")
    private String updaterName;

    @Schema(description = "课程名称信息")
    private List<CourseSimpleInfoVO> courses;

    @Schema(description = "选项", example = "[\"选项1\", \"选项2\"]")
    private List<String> options;

    @Schema(description = "答案,判断题，数组第一个如果是1，代表正确，其他代表错误", example = "[1, 2]")
    private List<Integer> answers;

    @Schema(description = "解析", example = "这是答案解析")
    private String analysis;

    @Schema(description = "课程id列表", example = "[101, 102]")
    private List<Long> courseIds;

    @Schema(description = "被引用次数", example = "10")
    private Integer useTimes;

    @Schema(description = "作答次数", example = "50")
    private Integer answerTimes;

    @Schema(description = "正确率", example = "0.85")
    private Double correctRate;
}
