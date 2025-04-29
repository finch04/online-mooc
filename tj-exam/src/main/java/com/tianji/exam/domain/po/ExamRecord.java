package com.tianji.exam.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.exam.enums.ExamType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 考试记录表
 * </p>
 *
 * @author 虎哥
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exam_record")
public class ExamRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型：1-考试、2-练习
     */
    private ExamType type;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 小节id
     */
    private Long sectionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 实际得分
     */
    private Integer score;

    /**
     * 正确答题数
     */
    private Integer correctQuestions;

    /**
     * 考试用时
     */
    private Integer duration;
    /**
     * 是否完成
     */
    private Boolean finished;

    /**
     * 开始时间
     */
    private LocalDateTime createTime;

    /**
     * 交卷时间
     */
    private LocalDateTime finishTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
