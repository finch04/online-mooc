package com.tianji.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.model.po.Dnu;
import com.tianji.data.model.po.LogAnalysisResult;
import com.tianji.data.model.po.UserProfile;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.CourseProfileVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.FunnelPlotChartsVO;
import com.tianji.data.model.vo.UserProfileVO;

import java.util.List;

/**
 * @Description：数据分析服务类接口
 */
public interface IAnalysisService {

    FunnelPlotChartsVO courseConversionDpv(FlowQuery query);

    EchartsVO courseDetailGenderDuv(FlowQuery query);

    EchartsVO courseDetailProvinceDuv(FlowQuery query);

    LogAnalysisResult analyzeLogs(List<BusinessLog> logs);

    PageDTO<UserProfileVO> getAnalysisResultByUser(PageQuery query);

    PageDTO<CourseProfileVO> getAnalysisResultByCourse(PageQuery query);
}
