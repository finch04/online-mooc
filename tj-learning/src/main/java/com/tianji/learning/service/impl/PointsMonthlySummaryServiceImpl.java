package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.utils.DateUtils;
import com.tianji.learning.domain.po.PointsMonthlySummary;
import com.tianji.learning.mapper.PointsMonthlySummaryMapper;
import com.tianji.learning.service.IPointsMonthlySummaryService;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsMonthlySummaryServiceImpl extends ServiceImpl<PointsMonthlySummaryMapper, PointsMonthlySummary> implements IPointsMonthlySummaryService {

    private final IPointsRecordService pointsRecordService;

    @Override
    public PointsMonthlySummary querySummaryByUserAndMonth(Long userId, String yearMonth) {
        return lambdaQuery()
                .eq(PointsMonthlySummary::getUserId, userId)
                .eq(PointsMonthlySummary::getYearMonth, yearMonth)
                .one();
    }

    @Override
    public PageDTO<PointsMonthlySummary> querySummaryByUser(Long userId, PageQuery query) {
        Page<PointsMonthlySummary> page = lambdaQuery()
                .eq(PointsMonthlySummary::getUserId, userId)
                .orderByDesc(PointsMonthlySummary::getYearMonth)
                .page(query.toMpPage());
        return PageDTO.of(page);
    }

//    @Override
//    public Integer queryUserTotalPoints(Long userId) {
//        return baseMapper.selectUserTotalPoints(userId);
//    }

    @Override
    public List<PointsMonthlySummary> queryRecentNSummaries(Long userId, Integer n) {
        LocalDate now = LocalDate.now();
        String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        return lambdaQuery()
                .eq(PointsMonthlySummary::getUserId, userId)
                .le(PointsMonthlySummary::getYearMonth, currentMonth)
                .orderByDesc(PointsMonthlySummary::getYearMonth)
                .last("LIMIT " + n)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateAndSaveMonthlySummary(Long userId, String yearMonth) {
        // 检查是否已生成
        PointsMonthlySummary summary = querySummaryByUserAndMonth(userId, yearMonth);
        if (summary != null) {
            return;
        }
        // 计算当月积分
        Integer earnedPoints = pointsRecordService.calculatePointsByUserAndMonth(userId, yearMonth, true);
        Integer usedPoints = pointsRecordService.calculatePointsByUserAndMonth(userId, yearMonth, false);
        Integer expiredPoints = 0; // 积分过期逻辑需要根据业务规则实现

        // 保存汇总记录
        summary = new PointsMonthlySummary()
                .setUserId(userId)
                .setYearMonth(yearMonth)
                .setPointsEarned(earnedPoints)
                .setPointsUsed(usedPoints)
                .setPointsExpired(expiredPoints)
                .setCreateTime(DateUtils.now());
        save(summary);
    }
}