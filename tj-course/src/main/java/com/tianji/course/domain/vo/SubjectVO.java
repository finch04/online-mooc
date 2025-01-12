package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目分页数据
 */
@Data
@Schema(description = "题目分页数据")
public class SubjectVO {
    @Schema(description = "题目id", example = "1")
    private Long id;

    @Schema(description = "名称", example = "Java题")
    private String name;

    @Schema(description = "所属分类,每一个分类三级分类中间使用/分开", example = "计算机科学/软件开发/编程")
    private List<String> cates;

    @Schema(description = "题目类型", example = "1")
    private Integer subjectType;

    @Schema(description = "题目类型描述", example = "单选题")
    private String subjectTypeDesc;

    @Schema(description = "题目难易度描述", example = "简单")
    private String difficultDesc;

    @Schema(description = "难易度,1：简单，2：中等，3：困难", example = "1")
    private String difficulty;

    @Schema(description = "分值", example = "5")
    private Integer score;

    @Schema(description = "使用次数", example = "10")
    private Integer useTimes;

    @Schema(description = "作答次数", example = "50")
    private Integer answerTimes;

    @Schema(description = "更新人", example = "张三")
    private String updaterName;

    @Schema(description = "更新时间", example = "2022-07-11T20:24:00")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间", example = "2022-07-11T20:24:00")
    private LocalDateTime createTime;

    @Schema(description = "选项", example = "[\"选项1\", \"选项2\"]")
    private List<String> options;

    @Schema(description = "答案,判断题，数组第一个如果是1，代表正确，其他代表错误", example = "[1, 2]")
    private List<Integer> answers;

    @Schema(description = "解析", example = "这是答案解析")
    private String analysis;

    @Schema(description = "正确率，百分号精确到小数点后一位", example = "85.0%")
    private String accuRate;
}
