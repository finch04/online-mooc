package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "积分榜赛季信息")
public class PointsBoardSeasonVO {

    @Schema(description = "赛季id，1，就是第1赛季")
    private Integer id;

    @Schema(description = "赛季名称，例如：第1赛季")
    private String name;

    @Schema(description = "赛季开始时间")
    private LocalDate beginTime;

    @Schema(description = "赛季结束时间")
    private LocalDate endTime;
}
