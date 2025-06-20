package com.tianji.data.influxdb.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UrlMetrics {
    private long totalVisits;       // 总访问量
    private long failedVisits;      // 失败访问量
}