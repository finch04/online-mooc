package com.tianji.exam.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试题目分页数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "考试题目分页数据")
public class QuestionPageVO {

    @Schema(description = "题目id")
    private Long id;

    @Schema(description = "题目名称，题干")
    private String name;

    @Schema(description = "题目类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题")
    private Integer type;

    @Schema(description = "课程三级分类的名称集合")
    private List<String> categories;

    @Schema(description = "难易度，1：简单，2：中等，3：困难")
    private Integer difficulty;

    @Schema(description = "分值")
    private Integer score;

    @Schema(description = "引用次数")
    private Integer useTimes;

    @Schema(description = "回答次数")
    private Integer answerTimes;

    @Schema(description = "更新人")
    private String updater;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
