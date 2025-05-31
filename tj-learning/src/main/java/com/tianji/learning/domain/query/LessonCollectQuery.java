package com.tianji.learning.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "我的收藏分页查询条件")
public class LessonCollectQuery extends PageQuery {
    @ApiModelProperty(value = "搜索关键字", example = "Redis")
    private String name;
}
