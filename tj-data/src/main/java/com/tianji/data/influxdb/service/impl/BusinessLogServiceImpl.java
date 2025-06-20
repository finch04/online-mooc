package com.tianji.data.influxdb.service.impl;

import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.ExceptionsUtil;
import com.tianji.data.constants.LogBusinessEnum;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.service.IBusinessLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName BusinessLogServiceImple.java
 * @Description 日志服务接口
 */
@Slf4j
@Service
public class BusinessLogServiceImpl implements IBusinessLogService {

    @Autowired
    private BusinessLogMapper businessLogMapper;

    @Override
    public Boolean createBusinessLog(BusinessLog businessLog) {
        try {
            //插入到influxDB中
            businessLogMapper.insertOne(businessLog);
            return true;
        } catch (Exception e) {
            log.error("数据埋点日志插入异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new CommonException(LogBusinessEnum.SAVE_FAIL.getMsg());
        }
    }
}
