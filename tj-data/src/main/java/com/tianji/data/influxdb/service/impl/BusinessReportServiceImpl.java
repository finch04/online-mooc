package com.tianji.data.influxdb.service.impl;

import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.ExceptionsUtil;
import com.tianji.data.constants.LogBusinessEnum;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.service.IBusinessLogService;
import com.tianji.data.influxdb.service.IBusinessReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName BusinessReportServiceImpl.java
 * @Description 日志持久化到MySQL接口
 */
@Slf4j
@Service
public class BusinessReportServiceImpl implements IBusinessReportService {

}
