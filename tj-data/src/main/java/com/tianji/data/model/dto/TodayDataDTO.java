package com.tianji.data.model.dto;

import lombok.Data;

@Data
public class TodayDataDTO {
    private Integer version;
    //访问量，万次单位
    private Double visits;
    //今日订单金额,万元单位
    private Double orderAmount;
    //今日订单笔数
    private Integer orderNum;
    //今日新增学员数
    private Integer stuNewNum;
}
