package com.tianji.data.influxdb.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.domain.UrlMetrics;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.EchartsVO;
import retrofit2.http.Url;

import java.util.List;

/**
 * @ClassName IBusinessLogService.java
 * @Description 日志服务接口
 */
public interface IUrlAnalysisService {

    /**
     * 分析指定url的访问数据
     * @param query
     * @return
     */
    PageDTO<BusinessLog> analyzeUrl(UrlPageQuery  query);

    /**
     * 分析模糊url的访问数据
     * @param query
     * @return
     */
    PageDTO<BusinessLog> analyzeUrlByLike(UrlPageQuery query);

    /**
     * 获取指定url的指标数据
     * @param query
     * @return
     */
    EchartsVO getMetricByUrl(UrlQuery query);
}
