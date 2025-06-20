package com.tianji.data.influxdb.service;

import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.domain.UrlMetrics;

import java.util.List;

/**
 * @ClassName IBusinessLogService.java
 * @Description 日志服务接口
 */
public interface IUrlAnalysisService {

    /**
     * 分析指定url的访问数据
     * @param url
     * @param beginTime
     * @param endTime
     * @return
     */
    List<BusinessLog> analyzeUrl(String url, String beginTime, String endTime);

    /**
     * 分析模糊url的访问数据
     * @param url
     * @param beginTime
     * @param endTime
     * @return
     */
    List<BusinessLog> analyzeUrlByLike(String url, String beginTime, String endTime);
}
