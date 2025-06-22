package com.tianji.data.influxdb.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.EchartsVO;

/**
 * @ClassName IFlowService.java
 * @Description 流量统计服务接口
 */
public interface IFlowService {

    EchartsVO base(FlowQuery query);
    EchartsVO urlVisits(FlowQuery query);
    EchartsVO urlErrors(FlowQuery query);

}
