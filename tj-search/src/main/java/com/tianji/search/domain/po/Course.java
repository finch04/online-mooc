package com.tianji.search.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.convert.DateFormatter;

import java.time.LocalDateTime;
import java.util.List;

// 添加ES文档注解（如果需要使用Spring Data ES的注解功能）
@Document(indexName = "course")
@Data
public class Course {
    @org.springframework.data.annotation.Id
    private Long id;

    /** 课程名称 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /** 一级分类id */
    @Field(type = FieldType.Long)
    private Long categoryIdLv1;

    /** 二级分类id */
    @Field(type = FieldType.Long)
    private Long categoryIdLv2;

    /** 三级分类id */
    @Field(type = FieldType.Long)
    private Long categoryIdLv3;

    /** 是否免费 */
    @Field(type = FieldType.Boolean)
    private Boolean free;

    /** 课程类型：1：直播课，2：录播课 */
    @Field(type = FieldType.Integer)
    private Integer type;

    /** 课程销量，报名人数 */
    @Field(type = FieldType.Integer)
    private Integer sold;

    /** 价格 */
    @Field(type = FieldType.Integer)
    private Integer price;

    /** 课程评分 */
    @Field(type = FieldType.Integer)
    private Integer score;

    /** 老师id */
    @Field(type = FieldType.Long)
    private Long teacher;

    /** 章节数量 */
    @Field(type = FieldType.Integer)
    private Integer sections;

    /** 课程封面 */
    @Field(type = FieldType.Keyword)
    private String coverUrl;


    /** 发布时间 - 简化配置 */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second,pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 更新时间 */
    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @JsonIgnore
    public List<Long> getCategoryIds(){
        return List.of(categoryIdLv1, categoryIdLv2, categoryIdLv3);
    }
}