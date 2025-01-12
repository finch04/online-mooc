package com.tianji.exam.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 题目分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "题目分页查询条件")
public class QuestionPageQuery extends PageQuery {
    @Schema(description = "三级分类id集合")
    private List<Long> cateIds;

    @Schema(description = "题目类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题")
    private List<Integer> types;

    @Schema(description = "难易度，1：简单，2：中等，3：困难")
    private Integer difficulty;

    @Schema(description = "题目名称关键字")
    private String keyword;

    @Schema(description = "题目录入者id")
    private Long creater;
}
