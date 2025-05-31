package com.tianji.message.domain.dto;/**
 * @author fsq
 * @date 2025/5/27 10:03
 */

import com.tianji.common.constants.RegexConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @Author: fsq
 * @Date: 2025/5/27 10:03
 * @Version: 1.0
 */

@Data
@ApiModel(description = "用户消息DTO")
public class UserSimpleDTO {
    @ApiModelProperty(value = "用户id", example = "1")
    private Long id;
    @ApiModelProperty(value = "用户名称/昵称", example = "李四")
    private String name;
    @ApiModelProperty(value = "头像", example = "default-user-icon.jpg")
    private String icon;
}
