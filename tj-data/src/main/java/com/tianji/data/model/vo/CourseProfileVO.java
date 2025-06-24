package com.tianji.data.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseProfileVO {
    @ApiModelProperty(value = "主键 ID")
    private Long id;
    @ApiModelProperty(value = "课程id")
    private Long courseId;
    @ApiModelProperty(value = "课程名称")
    private String name;
    @ApiModelProperty(value = "封面url")
    private String coverUrl;
    @ApiModelProperty(value = "价格")
    private Integer price;
    @ApiModelProperty(value = "是否是免费课程")
    private Boolean free;
    //课程画像  访问用户的性别标签
    @ApiModelProperty(value = "访问用户的性别标签")
    private String sexLabel;
    //课程画像  访问用户的省份标签 只存储前5个
    @ApiModelProperty(value = "访问用户的省份标签")
    @TableField(typeHandler = JacksonTypeHandler.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING) // 确保解析为字符类型
    private List<String> provinceLabels;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}