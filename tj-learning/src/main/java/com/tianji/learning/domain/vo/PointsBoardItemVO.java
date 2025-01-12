package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "积分榜单信息")
public class PointsBoardItemVO {
    @Schema(description = "积分值")
    private Integer points;
    @Schema(description = "名次")
    private Integer rank;
    @Schema(description = "学生姓名")
    private String name;
}
