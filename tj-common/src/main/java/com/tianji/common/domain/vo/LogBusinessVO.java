package com.tianji.common.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description：日志模块
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LogBusinessVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "请求id")
    private String requestId;

    @ApiModelProperty(value = "域名")
    private String host;

    @ApiModelProperty(value = "ip地址")
    private String hostAddress;

    @ApiModelProperty(value = "请求路径")
    private String requestUri;

    @ApiModelProperty(value = "请求方式")
    private String requestMethod;

    @ApiModelProperty(value = "请求body")
    private String requestBody;

    @ApiModelProperty(value = "应答body")
    private String responseBody;

    @ApiModelProperty(value = "应答code")
    private int responseCode;

    @ApiModelProperty(value = "应答msg")
    private String responseMsg;

    @ApiModelProperty(value = "响应时间")
    private long responseTime;


    @ApiModelProperty(value = "用户")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @ApiModelProperty(value = "用户名称")
    public String userName;

    @ApiModelProperty(value = "性别")
    public String sex;

    @ApiModelProperty(value = "省")
    public String province;

    @ApiModelProperty(value = "市")
    public String city;

    @ApiModelProperty(value = "角色id")
    private Long roleId;


}