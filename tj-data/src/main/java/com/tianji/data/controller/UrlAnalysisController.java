package com.tianji.data.controller;/**
 * @author fsq
 * @date 2025/6/21 10:28
 */

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.domain.UrlMetrics;
import com.tianji.data.influxdb.service.IUrlAnalysisService;
import com.tianji.data.model.dto.Top10DataSetDTO;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.Top10DataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/21 10:28
 * @Version: 1.0
 */
@RestController
@Api(tags = "URL数据分析相关接口")
@RequestMapping("/data/url")
public class UrlAnalysisController {

    @Autowired
    private IUrlAnalysisService urlAnalysisService;

    @GetMapping("/page/log")
    @ApiOperation("根据url获取日志")
    public PageDTO<BusinessLog> getLogsPageByUrl(UrlPageQuery query) {
        return urlAnalysisService.getLogsPageByUrl(query);
    }

    @GetMapping("/page/log/like")
    @ApiOperation("根据模糊url获取日志")
    public PageDTO<BusinessLog> getLogsPageByUrlByLike(UrlPageQuery query) {
        return urlAnalysisService.getLogsPageByUrlByLike(query);
    }

    @GetMapping("/metric")
    @ApiOperation("根据绝对url获取指标数据")
    public EchartsVO getMetricByUrl(UrlQuery query) {
        return urlAnalysisService.getMetricByUrl(query);
    }


}
