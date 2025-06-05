package com.tianji.exam.domain.query;/**
 * @author fsq
 * @date 2025/6/5 11:17
 */

import com.tianji.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/5 11:17
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "考试分页查询条件")
public class ExamPageQuery extends PageQuery {
    @ApiModelProperty("考试类型")
    private Integer type;
}
