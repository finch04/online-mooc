package com.tianji.data.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class UserProfileVO {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键 ID")
    private Long id;
    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "用户名称")
    private String userName;
    @ApiModelProperty(value = "性别：0-男性，1-女性")
    private Integer sex;
    @ApiModelProperty(value = "用户头像")
    private String icon;
    @ApiModelProperty(value = "省")
    private String province;
    //用户偏好  常访问课程id 前5
    @ApiModelProperty(value = "用户偏好  常访问课程id 前5")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> courseLabels;
    //用户偏好  付费课程还是免费课程
    @ApiModelProperty(value = "用户偏好  付费课程还是免费课程 0免费1付费")
    private Integer freeLabel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}