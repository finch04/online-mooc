package com.tianji.data.influxdb.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @Description：日志模块
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Measurement(name = "gateway_logs") // 测量名称，类似数据库表名
public class LogVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "请求id")
    @Column(tag = true) // 适合作为查询条件的字段设为tag
    private String requestId;

    @ApiModelProperty(value = "域名")
    @Column(tag = true)
    private String host;

    @ApiModelProperty(value = "ip地址")
    @Column(tag = true)
    private String hostAddress;

    @ApiModelProperty(value = "请求路径")
    @Column
    private String requestUri;

    @ApiModelProperty(value = "请求方式")
    @Column(tag = true) // HTTP方法适合作为tag
    private String requestMethod;

    @ApiModelProperty(value = "请求body")
    @Column
    private String requestBody;

    @ApiModelProperty(value = "应答body")
    @Column
    private String responseBody;

    @ApiModelProperty(value = "应答code")
    @Column
    private int responseCode;

    @ApiModelProperty(value = "应答msg")
    @Column
    private String responseMsg;

    @ApiModelProperty(value = "响应时间")
    @Column
    private long responseTime;


    @ApiModelProperty(value = "用户")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(tag = true) // 用户ID适合作为tag
    private Long userId;

    @ApiModelProperty(value = "用户名称")
    @Column
    private String userName;

    @ApiModelProperty(value = "上次浏览页面")
    @Column
    private String lastReadUrl;

    @Column(timestamp = true)
    private Instant logTime; // 确保时间戳精确到纳秒级别

    // 构造方法中自动设置唯一时间戳
    public LogVO(String requestId) {
        this.requestId = requestId;
        this.logTime = Instant.now(); // 使用系统当前时间（纳秒精度）
    }
}