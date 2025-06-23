package com.tianji.data.influxdb.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.tianji.data.utils.TimeHandlerUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;
import org.springframework.data.annotation.Transient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * BusinessLog.java
 * @describe: 数据埋点实体类--入库时间序列数据库
 */
@Data
@ToString
@Measurement(database = "point_data", name = "log", retentionPolicy = "rp_point")
@HeadRowHeight(25)     // 增加表头行高，提升可读性
@ContentRowHeight(20)
@HeadStyle
public class BusinessLog {

    @ExcelProperty(value = "请求ID", index = 0 )
    @ApiModelProperty(value = "请求id")
    @Column(name = "request_id")
    public String requestId;

    @ExcelProperty(value = "域名", index = 1)
    @ApiModelProperty(value = "域名")
    @Column(name = "host")
    public String host;

    @ExcelProperty(value = "IP地址", index = 2)
    @ApiModelProperty(value = "ip地址")
    @Column(name = "host_address")
    public String hostAddress;

    @ExcelProperty(value = "请求路径", index = 3)
    @ApiModelProperty(value = "请求路径")
    @Column(name = "request_uri", tag = true)
    public String requestUri;

    @ExcelProperty(value = "请求方式", index = 4)
    @ApiModelProperty(value = "请求方式")
    @Column(name = "request_method", tag = true)
    public String requestMethod;

    @ExcelProperty(value = "请求内容", index = 5)
    @ApiModelProperty(value = "请求body")
    @Column(name = "request_body")
    public String requestBody;

    @ExcelProperty(value = "响应内容", index = 6)
    @ApiModelProperty(value = "应答body")
    @Column(name = "response_body")
    public String responseBody;

    @ExcelProperty(value = "响应状态码", index = 7)
    @ApiModelProperty(value = "应答code")
    @Column(name = "response_code", tag = true)
    public String responseCode;

    @ExcelProperty(value = "响应消息", index = 8)
    @ApiModelProperty(value = "应答msg")
    @Column(name = "response_msg")
    public String responseMsg;

    @ExcelProperty(value = "响应时间(ms)", index = 9)
    @ApiModelProperty(value = "响应时间")
    @Column(name = "response_time", tag = true)
    private String responseTime;

    @ExcelProperty(value = "用户ID", index = 10)
    @ApiModelProperty(value = "用户id")
    @Column(name = "user_id")
    public String userId;

    @ExcelProperty(value = "用户名", index = 11)
    @ApiModelProperty(value = "用户名称")
    @Column(name = "user_name")
    public String userName;

    @ExcelProperty(value = "角色ID", index = 12)
    @ApiModelProperty(value = "角色id")
    @Column(name = "role_id")
    public String roleId;

    @ExcelProperty(value = "数据状态", index = 13)
    @ApiModelProperty(value = "数据标志位")
    @Column(name = "data_state", tag = true)
    public String dataState = "0";

    @ExcelProperty(value = "性别", index = 14)
    @ApiModelProperty(value = "性别")
    @Column(name = "sex")
    public String sex;

    @ExcelProperty(value = "省份", index = 15)
    @ApiModelProperty(value = "省")
    @Column(name = "province")
    public String province;

    @ExcelProperty(value = "城市", index = 16)
    @ApiModelProperty(value = "市")
    @Column(name = "city")
    public String city;

    @ExcelProperty(value = "记录时间", index = 17)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "数据时间戳（纳秒级）")
    @TimeColumn
    @Column(name = "time")
    private String time;
}