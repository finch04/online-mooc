package com.tianji.data.service.impl;

import com.tianji.data.mapper.BusinessLogMapper;
import com.tianji.data.model.po.CourseConversionDpv;
import com.tianji.data.model.po.CourseDetailGenderDuv;
import com.tianji.data.model.po.CourseDetailProvinceDuv;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.query.TimeRange;
import com.tianji.data.model.vo.AxisVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.FunnelPlotChartsIndexVO;
import com.tianji.data.model.vo.FunnelPlotChartsVO;
import com.tianji.data.model.vo.SerierVO;
import com.tianji.data.service.IAnalysisService;
import com.tianji.data.service.ICourseConversionDpvService;
import com.tianji.data.service.ICourseDetailGenderDuvService;
import com.tianji.data.service.ICourseDetailProvinceDuvService;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements IAnalysisService {

    private final ICourseConversionDpvService courseConversionDpvService;
    private final ICourseDetailGenderDuvService courseDetailGenderDuvService;
    private final ICourseDetailProvinceDuvService courseDetailProvinceDuvService;

    @Override
    public FunnelPlotChartsVO courseConversionDpv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<CourseConversionDpv> list = courseConversionDpvService.lambdaQuery()
                .gt(CourseConversionDpv::getReportTime, timeRange.getBegin())
                .le(CourseConversionDpv::getReportTime, timeRange.getEnd())
                .list();

        // 计算总浏览次数和总下单次数
        long totalBrowseDpv = list.stream().mapToLong(CourseConversionDpv::getDoBrowseDpv).sum();
        long totalOrderDpv = list.stream().mapToLong(CourseConversionDpv::getDoOrderDpv).sum();

        // 封装漏斗图数据
        List<String> labels = new ArrayList<>();
        List<FunnelPlotChartsIndexVO> values = new ArrayList<>();

        labels.add("课程浏览");
        values.add(FunnelPlotChartsIndexVO.builder().name("课程浏览").value(totalBrowseDpv).build());

        labels.add("课程下单");
        values.add(FunnelPlotChartsIndexVO.builder().name("课程下单").value(totalOrderDpv).build());

        return FunnelPlotChartsVO.builder()
                .label(labels)
                .value(values)
                .build();
    }

    @Override
    public EchartsVO courseDetailGenderDuv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<CourseDetailGenderDuv> list = courseDetailGenderDuvService.lambdaQuery()
                .gt(CourseDetailGenderDuv::getReportTime, timeRange.getBegin())
                .le(CourseDetailGenderDuv::getReportTime, timeRange.getEnd())
                .list();

        // 计算男性和女性的总访问数
        long totalManDpv = list.stream().mapToLong(CourseDetailGenderDuv::getManDpv).sum();
        long totalWomanDpv = list.stream().mapToLong(CourseDetailGenderDuv::getWomanDpv).sum();

        // 创建 EchartsVO 对象
        EchartsVO echartsVO = new EchartsVO();

        // 创建饼图系列数据
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("name", "男", "value", totalManDpv));
        data.add(Map.of("name", "女", "value", totalWomanDpv));

        SerierVO serierVO = SerierVO.builder()
                .name("课程详情访问数")
                .type(SerierVO.TYPE_PIE)
                .data(data)
                .build();

        echartsVO.setSeries(Collections.singletonList(serierVO));
        return echartsVO;
    }

    @Override
    public EchartsVO courseDetailProvinceDuv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<CourseDetailProvinceDuv> list = courseDetailProvinceDuvService.lambdaQuery()
                .gt(CourseDetailProvinceDuv::getReportTime, timeRange.getBegin())
                .le(CourseDetailProvinceDuv::getReportTime, timeRange.getEnd())
                .list();

        // 按省份分组并计算总访问数，取前10
        List<Map.Entry<String, Long>> top10Provinces = list.stream()
                .collect(Collectors.groupingBy(
                        CourseDetailProvinceDuv::getProvinceName,
                        Collectors.summingLong(CourseDetailProvinceDuv::getDuv)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // 创建 EchartsVO 对象
        EchartsVO echartsVO = new EchartsVO();

        // 创建 x 轴数据
        List<AxisVO> xAxis = new ArrayList<>();
        List<String> provinceNames = top10Provinces.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        xAxis.add(AxisVO.builder()
                .type(AxisVO.TYPE_CATEGORY)
                .data(provinceNames)
                .build());

        // 创建 y 轴数据
        List<AxisVO> yAxis = new ArrayList<>();
        yAxis.add(AxisVO.builder()
                .type(AxisVO.TYPE_VALUE)
                .build());

        // 创建柱状图系列数据
        List<Long> duvValues = top10Provinces.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        SerierVO serierVO = SerierVO.builder()
                .name("课程详情访问数")
                .type(SerierVO.TYPE_BAR)
                .data(duvValues)
                .build();

        echartsVO.setXAxis(xAxis);
        echartsVO.setYAxis(yAxis);
        echartsVO.setSeries(Collections.singletonList(serierVO));
        return echartsVO;
    }
    /**
     * 获取时间范围
     */
    private TimeRange getTimeRange(FlowQuery query) {
        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();

        if (query.getBeginTime() != null && query.getEndTime() != null) {
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }

        return new TimeRange(beginTime, endTime);
    }
}