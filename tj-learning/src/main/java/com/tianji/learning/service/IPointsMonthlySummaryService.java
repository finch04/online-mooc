package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.PointsMonthlySummary;

import java.util.List;

public interface IPointsMonthlySummaryService extends IService<PointsMonthlySummary> {

    PointsMonthlySummary querySummaryByUserAndMonth(Long userId, String yearMonth);

    PageDTO<PointsMonthlySummary> querySummaryByUser(Long userId, PageQuery query);

//    Integer queryUserTotalPoints(Long userId);

    List<PointsMonthlySummary> queryRecentNSummaries(Long userId, Integer n);

    void calculateAndSaveMonthlySummary(Long userId, String yearMonth);
}