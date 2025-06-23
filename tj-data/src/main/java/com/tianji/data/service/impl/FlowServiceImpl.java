package com.tianji.data.service.impl;

import com.tianji.common.utils.NumberUtils;
import com.tianji.data.mapper.FlowMapper;
import com.tianji.data.model.po.DauProvince;
import com.tianji.data.model.po.DpvTime;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.AxisVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.SerierVO;
import com.tianji.data.service.IDauProvinceService;
import com.tianji.data.service.IDpvTimeService;
import com.tianji.data.service.IFlowService;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowServiceImpl implements IFlowService {

    private final FlowMapper flowMapper;
    private final IDauProvinceService dauProvinceService;
    private final IDpvTimeService dpvTimeService;

    @Override
    public EchartsVO dnu(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<Long> data = flowMapper.dnuByDay(timeRange.begin, timeRange.end);
        return createTimeSeriesChart("每日新增用户数", "用户数", "bar", data, timeRange);
    }

    @Override
    public EchartsVO dpv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<Long> data = flowMapper.dpvByDay(timeRange.begin, timeRange.end);
        return createTimeSeriesChart("页面浏览量趋势", "浏览量", "line", data, timeRange);
    }

    @Override
    public EchartsVO duv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<Long> data = flowMapper.duvByDay(timeRange.begin, timeRange.end);
        return createTimeSeriesChart("独立访客数趋势", "访客数", "line", data, timeRange);
    }

    @Override
    public EchartsVO dau(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<Long> data = flowMapper.allDauForUserIdByDay(timeRange.begin, timeRange.end);
        return createTimeSeriesChart("日活跃用户数趋势", "用户数", "bar", data, timeRange);
    }

    @Override
    public EchartsVO dpvTime(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<DpvTime> list = dpvTimeService.lambdaQuery()
                .gt(DpvTime::getReportTime, timeRange.begin)
                .le(DpvTime::getReportTime, timeRange.end)
                .list();

        // 统计每个时段的总活跃访问量
        long midNightTotal = 0;
        long noonTotal = 0;
        long afternoonTotal = 0;
        long eveningTotal = 0;

        for (DpvTime dpvTime : list) {
            midNightTotal += dpvTime.getMidNightDpv();
            noonTotal += dpvTime.getNoonDpv();
            afternoonTotal += dpvTime.getAfternoonDpv();
            eveningTotal += dpvTime.getEveningDpv();
        }

        // 整理数据用于饼图
        List<Map<String, Object>> pieData = new ArrayList<>();
        pieData.add(Map.of("name", "0~7点", "value", midNightTotal));
        pieData.add(Map.of("name", "7~12点", "value", noonTotal));
        pieData.add(Map.of("name", "12~18点", "value", afternoonTotal));
        pieData.add(Map.of("name", "18~24点", "value", eveningTotal));

        // 创建饼图系列
        SerierVO series = SerierVO.builder()
                .name("用户活跃访问量时段分布")
                .type(SerierVO.TYPE_PIE)
                .data(pieData)
                .build();

        // 创建 EchartsVO
        EchartsVO chart = new EchartsVO();
        chart.setSeries(Collections.singletonList(series));
        chart.setXAxis(null);
        chart.setYAxis(null);

        return chart;
    }

    @Override
    public EchartsVO dauProvince(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<DauProvince> list = dauProvinceService.lambdaQuery()
                .gt(DauProvince::getReportTime, timeRange.begin)
                .le(DauProvince::getReportTime, timeRange.end)
                .list();

        // 按省份分组统计活跃用户数
        Map<String, Long> provinceDauMap = list.stream()
                .collect(Collectors.groupingBy(DauProvince::getProvince, Collectors.summingLong(DauProvince::getDau)));

        // 对统计结果按活跃用户数降序排序，取前 10 条记录
        List<Map.Entry<String, Long>> top10Provinces = provinceDauMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // 构建柱状图数据
        List<String> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        for (Map.Entry<String, Long> entry : top10Provinces) {
            xData.add(entry.getKey());
            yData.add(entry.getValue().doubleValue());
        }

        // 配置 X 轴（分类轴）
        AxisVO xAxis = AxisVO.builder()
                .type(AxisVO.TYPE_CATEGORY)
                .data(xData)
                .build();

        // 处理图表数据
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        double max = yData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double min = yData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double average = yData.isEmpty() ? 0 : NumberUtils.average(yData);

        series.add(new SerierVO(
                "活跃用户省排名前 10",
                "bar",
                yData,
                String.format("%.0f 人", max),
                String.format("%.0f 人", min)
        ));

        double effectiveMin = min == 0 ? 0 : min * 0.9;
        yAxis.add(AxisVO.builder()
                .max(max)
                .min(effectiveMin)
                .interval(calculateInterval(max, effectiveMin))
                .average(NumberUtils.setScale(average))
                .type(AxisVO.TYPE_VALUE)
                .build());

        EchartsVO chart = new EchartsVO();
        chart.setXAxis(Collections.singletonList(xAxis));
        chart.setYAxis(yAxis);
        chart.setSeries(series);

        return chart;
    }

    /**
     * 创建时间序列图表
     */
    private EchartsVO createTimeSeriesChart(String title, String yAxisName, String chartType,
                                            List<Long> data, TimeRange timeRange) {
        EchartsVO chart = new EchartsVO();

        // 设置 X 轴为日期范围
        AxisVO xAxis = AxisVO.ofDateRange(timeRange.begin, timeRange.end);

        // 处理数据并设置 Y 轴
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // 确保数据长度与日期范围一致
        List<Long> validData = data != null ? data : new ArrayList<>();
        while (validData.size() < xAxis.getData().size()) {
            validData.add(0L);
        }

        // 转换数据类型并计算统计值
        List<Double> doubleData = validData.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());

        double max = doubleData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double min = doubleData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double average = doubleData.isEmpty() ? 0 : NumberUtils.average(doubleData);

        // 添加系列数据
        series.add(new SerierVO(
                title,
                chartType,
                doubleData,
                String.format("%.0f 次", max),
                String.format("%.0f 次", min)
        ));

        // 添加 Y 轴配置
        double effectiveMin = min == 0 ? 0 : min * 0.9;
        yAxis.add(AxisVO.builder()
                .max(max)
                .min(effectiveMin)
                .interval(calculateInterval(max, effectiveMin))
                .average(NumberUtils.setScale(average))
                .type(AxisVO.TYPE_VALUE)
                .build());

        // 设置图表数据
        chart.setXAxis(Collections.singletonList(xAxis));
        chart.setYAxis(yAxis);
        chart.setSeries(series);

        return chart;
    }

    /**
     * 构建 URL 统计图表
     */
    private EchartsVO buildUrlChart(Map<String, Long> dataMap, String title, String yAxisName) {
        if (dataMap.isEmpty()) {
            return new EchartsVO();
        }

        EchartsVO chart = new EchartsVO();
        List<String> xData = new ArrayList<>(dataMap.keySet());
        List<Double> yData = dataMap.values().stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());

        // 配置 X 轴（分类轴）
        AxisVO xAxis = AxisVO.builder()
                .type(AxisVO.TYPE_CATEGORY)
                .data(xData)
                .build();

        // 处理图表数据
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        double max = yData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double min = yData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double average = yData.isEmpty() ? 0 : NumberUtils.average(yData);

        series.add(new SerierVO(
                title,
                "bar",
                yData,
                String.format("%.0f 次", max),
                String.format("%.0f 次", min)
        ));

        double effectiveMin = min == 0 ? 0 : min * 0.9;
        yAxis.add(AxisVO.builder()
                .max(max)
                .min(effectiveMin)
                .interval(calculateInterval(max, effectiveMin))
                .average(NumberUtils.setScale(average))
                .type(AxisVO.TYPE_VALUE)
                .build());

        chart.setXAxis(Collections.singletonList(xAxis));
        chart.setYAxis(yAxis);
        chart.setSeries(series);

        return chart;
    }

    /**
     * 解析 URL 统计数据
     */
    private Map<String, Long> parseUrlData(List<String> data) {
        Map<String, Long> result = new HashMap<>();
        if (data == null || data.isEmpty()) {
            return result;
        }

        for (String item : data) {
            String[] parts = item.split(",");
            if (parts.length >= 2) {
                result.put(parts[0], Long.parseLong(parts[1]));
            }
        }
        return result;
    }

    /**
     * 计算 Y 轴间隔
     */
    private double calculateInterval(double max, double min) {
        double range = max - min;
        return range <= 0 ? 1 : Math.ceil(range / 5);
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

    /**
     * 时间范围内部类
     */
    private static class TimeRange {
        String begin;
        String end;

        public TimeRange(String begin, String end) {
            this.begin = begin;
            this.end = end;
        }
    }
}