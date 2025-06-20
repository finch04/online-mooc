package com.tianji.data.influxdb.domain;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * BusinessLog.java
 * @describe: 数据埋点实体类--入库时间序列数据库
 */
@Data
@ToString
@Measurement(database = "point_data", name = "log")
public class BusinessLog {

    @ApiModelProperty(value = "请求id")
    @Column(name = "request_id")
    public String requestId;

    @ApiModelProperty(value = "域名")
    @Column(name = "host")
    public String host;

    @ApiModelProperty(value = "ip地址")
    @Column(name = "host_address")
    public String hostAddress;

    @ApiModelProperty(value = "请求路径")
    @Column(name = "request_uri", tag = true)
    public String requestUri;

    @ApiModelProperty(value = "请求方式")
    @Column(name = "request_method", tag = true)
    public String requestMethod;

    @ApiModelProperty(value = "请求body")
    @Column(name = "request_body")
    public String requestBody;

    @ApiModelProperty(value = "应答body")
    @Column(name = "response_body")
    public String responseBody;

    @ApiModelProperty(value = "应答code")
    @Column(name = "response_code", tag = true)
    public String responseCode;

    @ApiModelProperty(value = "应答msg")
    @Column(name = "response_msg")
    public String responseMsg;

    @ApiModelProperty(value = "用户")
    @Column(name = "user_id")
    public String userId;

    @ApiModelProperty(value = "用户名称")
    @Column(name = "user_name")
    public String userName;

    @ApiModelProperty(value = "业务类型")
    @Column(name = "business_type")
    public String businessType;

    @ApiModelProperty(value = "设备号")
    @Column(name = "device_number")
    public String deviceNumber;

    @ApiModelProperty(value = "企业编号")
    @Column(name = "company_no")
    public String companyNO;

    @ApiModelProperty(value = "性别")
    @Column(name = "sex")
    public String sex;

    @ApiModelProperty(value = "修改时间")
    @Column(name = "create_by")
    public Long createBy;

    @ApiModelProperty(value = "修改时间")
    @Column(name = "update_by")
    public Long updateBy;

    @ApiModelProperty(value = "数据标志位")
    @Column(name = "data_state", tag = true)
    public String dataState ="0";

    @ApiModelProperty(value = "省")
    @Column(name = "province")
    public String province;

    @ApiModelProperty(value = "市")
    @Column(name = "city")
    public String city;

}
