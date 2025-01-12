package com.tianji.learning.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "互动回答分页查询条件")
public class ReplyPageQuery extends PageQuery {
    @Schema(description = "问题id，不为空则代表根据问题查询回答")
    private Long questionId;
    @Schema(description = "回答id，不为空则代表根据回答查询评论")
    private Long answerId;
}
