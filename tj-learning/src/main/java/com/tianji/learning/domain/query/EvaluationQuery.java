package com.tianji.learning.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程评价查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("课程评价查询条件")
public class EvaluationQuery extends PageQuery {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("教师ID")
    private Long teacherId;

    @ApiModelProperty(value = "是否只查询我的评价", example = "1")
    private Boolean onlyMine = false;

}    