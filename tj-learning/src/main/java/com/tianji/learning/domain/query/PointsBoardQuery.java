package com.tianji.learning.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "积分排行榜分页查询条件")
public class PointsBoardQuery extends PageQuery {
    @Schema(description = "赛季id，为null或者0则代表查询当前赛季")
    private Long season;
}
