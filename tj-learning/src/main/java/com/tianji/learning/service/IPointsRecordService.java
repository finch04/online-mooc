package com.tianji.learning.service;

import com.tianji.learning.domain.po.PointsRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.msg.SignInMessage;

import java.util.List;

/**
 * <p>
 * 学习积分记录，每个月底清零 服务类
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
public interface IPointsRecordService extends IService<PointsRecord> {
    //增加积分
    void addPointsRecord(SignInMessage msg, PointsRecordType type);

    //查询我的今日积分情况
    List<PointsStatisticsVO> queryMyTodayPoints();

    /**
     * 获取用户当前可用积分
     */
    Integer getUserCurrentPoints(Long userId);

    /**
     * 消费积分
     */
    void consumePoints(Long userId, Integer points, String description);

    /**
     * 计算用户某个月的积分（获取/使用）
     * @param userId 用户ID
     * @param yearMonth 年月（YYYYMM）
     * @param isEarn 是否为获取积分（true-获取，false-使用）
     */
    Integer calculatePointsByUserAndMonth(Long userId, String yearMonth, boolean isEarn);

}
