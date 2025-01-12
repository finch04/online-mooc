package com.tianji.message.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知模板查询对象")
@Data
public class NoticeTemplatePageQuery extends PageQuery {
    private Integer status;
    private String keyword;
}
