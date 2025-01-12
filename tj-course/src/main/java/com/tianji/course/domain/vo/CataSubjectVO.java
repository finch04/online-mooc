package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 目录和习题模型
 */
@Data
@Schema(description = "课程题目统计")
public class CataSubjectVO {
    @Schema(description = "小节或测试id", example = "1")
    private Long cataId;

    @Schema(description = "小节或测试名称", example = "第一章第一节")
    private String cataName;

    @Schema(description = "类型，2：节，3：测试", example = "2")
    private Integer type;

    @Schema(description = "题目数量", example = "5")
    private Integer subjectNum;

    @Schema(description = "题目总分", example = "25")
    private Integer subjectScore;
}
