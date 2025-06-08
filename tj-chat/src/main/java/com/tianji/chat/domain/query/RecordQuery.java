package com.tianji.chat.domain.query;

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
@ApiModel("历史记录查询条件")
public class RecordQuery extends PageQuery {

    @ApiModelProperty("会话ID")
    private String sessionId;

}    