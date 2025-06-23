package com.tianji.data.service;

import com.tianji.data.influxdb.domain.BusinessLog;

import java.util.List;

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

    /**
     * 批量持久化日志信息
     * @param voList 日志对象列表
     * @return 是否执行成功
     */
    Boolean createBusinessLogBatch(List<BusinessLog> voList);
}
