package com.tianji.user.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "用户分页查询条件")
public class UserSimplePageQuery extends PageQuery {
    @ApiModelProperty(value = "性别")
    private Integer gender;
    @ApiModelProperty(value = "用户名称")
    private String name;
    @ApiModelProperty(value = "用户类型")
    private String type;
}
