package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "积分榜单汇总信息")
public class PointsBoardVO {
    @Schema(description = "我的榜单排名")
    private Integer rank;
    @Schema(description = "我的积分值")
    private Integer points;
    @Schema(description = "前100名上榜人信息")
    private List<PointsBoardItemVO> boardList;
}
