package com.tianji.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户信息")
public class UserPageVO {
    @ApiModelProperty(value = "用户id", example = "1")
    private Long id;
    @ApiModelProperty(value = "用户名称", example = "张三")
    private String name;
    @ApiModelProperty(value = "头像", example = "default-icon.jpg")
    private String icon;
    @ApiModelProperty(value = "手机号", example = "13800010004")
    private String cellPhone;
    @ApiModelProperty(value = "注册时间", example = "2022-07-12")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "账户状态，0-禁用，1-正常", example = "1")
    private Integer status;
}