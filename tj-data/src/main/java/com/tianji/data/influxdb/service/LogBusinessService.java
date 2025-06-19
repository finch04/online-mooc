package com.tianji.data.influxdb.service;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.data.influxdb.domain.LogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogBusinessService {

    private final InfluxDBClient influxDBClient;

    @Value("${spring.influx.org}")
    private String org;

    @Value("${spring.influx.bucket}")
    private String bucket;


    @Autowired
    public LogBusinessService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }


    // 新增单个日志记录 - 接收common模块的VO
    public void saveLog(LogBusinessVO commonLog) {
        LogVO vo = convertToAnnotatedLog(commonLog);
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(bucket, org, WritePrecision.MS, vo);
    }

    // 批量新增日志记录 - 接收common模块的VO列表
    public void saveLogs(List<LogBusinessVO> commonLogs) {
        List<LogVO> logs = commonLogs.stream()
                .map(this::convertToAnnotatedLog)
                .collect(Collectors.toList());
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurements(bucket, org, WritePrecision.MS, logs);
    }

    // 将common模块的VO转换为带注解的VO
    private LogVO convertToAnnotatedLog(LogBusinessVO commonLog) {
        LogVO annotatedLog = new LogVO();
        // 复制属性值
        BeanUtils.copyProperties(commonLog, annotatedLog);
        // 如果有特殊属性需要单独处理，可以在这里添加
        return annotatedLog;
    }

    // 删除日志记录
    public void deleteLog(String requestId) {
        DeleteApi deleteApi = influxDBClient.getDeleteApi();
        // 这里假设requestId是唯一标识，您可以根据实际需求调整删除条件
        deleteApi.delete(OffsetDateTime.from(Instant.now().minus(30, ChronoUnit.DAYS)),
                OffsetDateTime.from(Instant.now()),
                      "requestId=\"" + requestId + "\"", 
                      bucket, 
                      org);
    }

    // 按时间范围查询日志
    public List<LogBusinessVO> queryLogsByTimeRange(Instant start, Instant end) {
        String fluxQuery = String.format("from(bucket:\"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"log_business\") " +
                        "|> map(fn: (r) => ({ r with _time: r._time + 8h }))", // 时区转换
                bucket, start, end);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, org);
        return tables.stream()
                .flatMap(table -> table.getRecords().stream())
                .map(this::recordToLog)
                .collect(Collectors.toList());
    }


    // 将FluxRecord转换为LogBusinessVO
    private LogBusinessVO recordToLog(FluxRecord record) {
        LogBusinessVO vo = new LogBusinessVO();
        vo.setRequestId((String) record.getValueByKey("requestId"));
        vo.setHost((String) record.getValueByKey("host"));
        vo.setHostAddress((String) record.getValueByKey("hostAddress"));
        vo.setRequestUri((String) record.getValueByKey("requestUri"));
        vo.setRequestMethod((String) record.getValueByKey("requestMethod"));
        vo.setRequestBody((String) record.getValueByKey("requestBody"));
        vo.setResponseBody((String) record.getValueByKey("responseBody"));
        vo.setResponseCode((int) record.getValueByKey("responseCode"));
        vo.setResponseMsg((String) record.getValueByKey("responseMsg"));
        vo.setResponseTime((long) record.getValueByKey("responseTime"));
        vo.setUserId(Long.parseLong((String) record.getValueByKey("userId")));
        vo.setUserName((String) record.getValueByKey("userName"));
        vo.setLastReadUrl((String) record.getValueByKey("lastReadUrl"));
        return vo;
    }
}