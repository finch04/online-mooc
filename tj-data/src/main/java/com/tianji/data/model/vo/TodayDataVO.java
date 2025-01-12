package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 今日数据
 */
@Data
public class TodayDataVO {
    @Schema(description = "访问量，万次单位")
    private Double visits;
    @Schema(description = "今日订单金额,万元单位")
    private Double orderAmount;
    @Schema(description = "今日订单笔数")
    private Integer orderNum;
    @Schema(description = "今日新增学员数")
    private Integer stuNewNum;
}
