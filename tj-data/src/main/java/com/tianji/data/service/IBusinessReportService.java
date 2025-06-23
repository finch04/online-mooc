package com.tianji.data.service;

import com.tianji.data.influxdb.domain.BusinessLog;

import java.util.List;

/**
 * @ClassName IBusinessReportService.java
 * @Description 日志持久化到MySQL接口
 */
public interface IBusinessReportService {

    void saveLogs(List<BusinessLog> list);
}
