package com.tianji.learning.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习笔记分页查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "学习笔记分页查询条件")
public class NotePageQuery extends PageQuery {
    // 用户端查询条件
    @Schema(description = "课程id")
    private Long courseId;

    @Schema(description = "小节id", example = "1")
    private Long sectionId;

    @Schema(description = "是否只查询我的笔记", example = "1")
    private Boolean onlyMine = false;
}
