package com.tianji.user.domain.dto;/**
 * @author fsq
 * @date 2025/5/21 13:13
 */

/**
 * @Author: fsq
 * @Date: 2025/5/21 13:13
 * @Version: 1.0
 */



import com.tianji.common.constants.RegexConstants;
import com.tianji.common.validate.annotations.EnumValid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "学员修改个人信息")
public class StudentUpdateDTO {
    @ApiModelProperty(value = "用户id", example = "1")
    private Long id;
    @ApiModelProperty(value = "用户名称/昵称", example = "李四")
    private String name;
    @ApiModelProperty(value = "头像", example = "default-user-icon.jpg")
    private String icon;
    @ApiModelProperty(value = "个人介绍", example = "高级Java讲师")
    private String intro;
    @ApiModelProperty(value = "形象照地址", example = "default-teacher-photo.jpg")
    private String photo;
    @ApiModelProperty(value = "邮箱")
    @Email(message = "邮箱格式有误！")
    private String email;
    @ApiModelProperty(value = "QQ号码")
    private String qq;
    @ApiModelProperty(value = "省")
    private String province;
    @ApiModelProperty(value = "市")
    private String city;
    @ApiModelProperty(value = "区")
    private String district;
    @ApiModelProperty(value = "性别：0-男性，1-女性", example = "0")
    @EnumValid(enumeration = {0, 1}, message = "性别格式不正确")
    private Integer gender;
}
