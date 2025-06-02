package com.tianji.media.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "文件搜索条件")
public class FileQuery extends PageQuery {
    @ApiModelProperty("文件名称关键字")
    private String name;
}
