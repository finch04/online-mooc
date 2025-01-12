package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 考试记录表
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-18
 */
@Data
@Schema(description = "考试问题详情")
public class SubjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "问题id")
    private Long id;

    @Schema(description = "题干")
    private String name;

    @Schema(description = "选择题的选项")
    private List<String> options;

    @Schema(description = "分值")
    private Integer score;

    @Schema(description = "问题类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题")
    private Integer subjectType;

    @Schema(description = "难易度，1：简单，2：中等，3：困难")
    private Integer difficulty;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "选择题答案，0对应A，1对应B，可填多个")
    private List<Integer> answers;
}
