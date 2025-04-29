package com.tianji.exam.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 考试记录明细，学生答案
 * </p>
 *
 * @author 虎哥
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exam_record_detail")
public class ExamRecordDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试记录id
     */
    private Long examId;

    /**
     * 问题id
     */
    private Long questionId;

    /**
     * 是否正确
     */
    private Boolean correct;

    /**
     * 本题得分
     */
    private Integer score;

    /**
     * 考生答案
     */
    private String answer;

    /**
     * 教师评语
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
