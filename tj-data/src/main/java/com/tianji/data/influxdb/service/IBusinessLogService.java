package com.tianji.data.influxdb.service;

import com.tianji.data.influxdb.domain.BusinessLog;

/**
 * @ClassName IBusinessLogService.java
 * @Description 日志服务接口
 */
public interface IBusinessLogService {

    /**
     * 持久化日志信息
     * @param businessLog 日志对象
     * @return 是否执行成功
     */
    Boolean createBusinessLog(BusinessLog businessLog);
}
