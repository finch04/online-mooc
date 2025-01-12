package com.tianji.message.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知模板查询对象")
@Data
public class NoticeTaskPageQuery extends PageQuery {
    private Boolean finished;
    private String keyword;
    private LocalDateTime minPushTime;
    private LocalDateTime maxPushTime;
}
