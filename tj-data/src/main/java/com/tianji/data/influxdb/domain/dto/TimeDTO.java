package com.tianji.data.influxdb.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TimeDTO
 *
 * @author: fsq
 * @describe: 日期对象类
 * @date: 2022/12/28 10:10
 */
@Data
public class TimeDTO {


    /**
     * 目标日期：LocalDate
     */
    private LocalDate targetDate;

    /**
     * 目标日期：LocalDateTime
     */
    private LocalDateTime targetDateTime;

    /**
     * 目标日期开始 字符串
     */
    private String begin;

    /**
     * 目标日期结束时间 字符串
     */
    private String end;

    /**
     * 目标日期开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 目标日期结束时间
     */
    private LocalDateTime endTime;


    /**
     * 获取日志格式化
     * @return
     */
    public DateTimeFormatter getTimeFormatter(){
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}
