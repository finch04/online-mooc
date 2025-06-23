package com.tianji.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.model.po.Dnu;
import com.tianji.data.model.po.LogAnalysisResult;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.FunnelPlotChartsVO;

import java.util.List;

/**
 * @Description：数据分析服务类接口
 */
public interface IAnalysisService {


    FunnelPlotChartsVO courseConversionDpv(FlowQuery query);

    EchartsVO courseDetailGenderDuv(FlowQuery query);

    EchartsVO courseDetailProvinceDuv(FlowQuery query);

    LogAnalysisResult analyzeLogs(List<BusinessLog> logs);
}
