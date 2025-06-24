package com.tianji.data.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "course_profile", autoResultMap = true)
@ApiModel(value="CourseProfile对象", description="课程画像")
public class CourseProfile {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键 ID")
    private Long id;
    @ApiModelProperty(value = "课程id")
    private Long courseId;
    //课程画像  访问用户的性别标签
    @ApiModelProperty(value = "访问用户的性别标签")
    private String sexLabel;
    //课程画像  访问用户的省份标签 只存储前5个
    @ApiModelProperty(value = "访问用户的省份标签")
    @JsonFormat(shape = JsonFormat.Shape.STRING) // 确保解析为字符类型
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> provinceLabels;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}