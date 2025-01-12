package com.tianji.api.dto.exam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "题目数据")
public class QuestionDTO {

    @Schema(description = "题目id")
    private Long id;

    @Schema(description = "题目名称，题干")
    private String name;

    @Schema(description = "题目类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题")
    private String type;

    @Schema(description = "难易度，1：简单，2：中等，3：困难")
    private Integer difficulty;

    @Schema(description = "分值")
    private Integer score;

    @Schema(description = "选择题选项，json数组格式")
    private List<String> options;

    @Schema(description = "选择题正确答案1到10，如果有多个答案，中间使用逗号隔开，如果是判断题，1：代表正确，其他代表错误")
    private String answer;

    @Schema(description = "答案解析")
    private String analysis;
}
