package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "每日积分统计实体")
public class PointsStatisticsVO {
    @Schema(description = "获取积分方式")
    private String type;
    @Schema(description = "今日已获取积分值")
    private Integer points;
    @Schema(description = "单日积分上限")
    private Integer maxPoints;
}
