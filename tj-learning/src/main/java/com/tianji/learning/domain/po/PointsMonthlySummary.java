package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户月度积分汇总表
 * </p>
 *
 * @author 参考示例
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_monthly_summary")
public class PointsMonthlySummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 年月（YYYYMM）
     */
    private String yearMonth;

    /**
     * 当月获得积分
     */
    private Integer pointsEarned;

    /**
     * 当月使用积分
     */
    private Integer pointsUsed;

    /**
     * 当月过期积分
     */
    private Integer pointsExpired;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}