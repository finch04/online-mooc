package com.tianji.learning.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

/**
 * 互动问题表单信息
 */
@Data
@Schema(description = "互动问题表单信息")
public class QuestionFormDTO {
    @Schema(description = "课程id")
    @NotNull(message = "课程id不能为空")
    private Long courseId;

    @Schema(description = "章id")
    @NotNull(message = "章id不能为空")
    private Long chapterId;

    @Schema(description = "小节id")
    @NotNull(message = "小节id不能为空")
    private Long sectionId;

    @Schema(description = "标题")
    @NotNull(message = "标题不能为空")
    @Length(min = 1, max = 254, message = "标题长度太长")
    private String title;

    @Schema(description = "互动问题描述")
    @NotNull(message = "问题描述不能为空")
    private String description;

    @Schema(description = "是否匿名提问")
    private Boolean anonymity;
}
