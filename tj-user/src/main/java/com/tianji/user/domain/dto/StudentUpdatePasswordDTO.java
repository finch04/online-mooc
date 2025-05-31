package com.tianji.user.domain.dto;/**
 * @author fsq
 * @date 2025/5/21 14:51
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: fsq
 * @Date: 2025/5/21 14:51
 * @Version: 1.0
 */
@Data
@ApiModel(description = "学生修改密码的表单实体")
public class StudentUpdatePasswordDTO {

    @ApiModelProperty(value = "用户id", example = "1")
    @NotNull
    private Long id;
    @ApiModelProperty(value = "原始密码", example = "123321")
    @NotNull
    private String oldPassword;
    @ApiModelProperty(value = "新密码", example = "123321")
    @NotNull
    private String newPassword;
}
