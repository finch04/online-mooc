package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程评价实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("evaluation")
public class Evaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键，互动问题的id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    private Long userId;

    private Long courseId;

    private Long teacherId;

    private Integer contentRating;

    private Integer teachingRating;

    private Integer difficultyRating;

    private Integer valueRating;

    private String comment;

    @TableField("is_anonymous")
    private Boolean anonymity;

    private Integer helpCount;

    private Boolean hidden;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}    