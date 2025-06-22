package com.tianji.data.influxdb.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.NumberUtils;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.mapper.FlowMapper;
import com.tianji.data.influxdb.service.IFlowService;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.AxisVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.SerierVO;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowServiceImpl implements IFlowService {

    private final FlowMapper flowMapper;

    @Override
    public EchartsVO base(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);

        List<Long> totalDnu = flowMapper.dnuByDay(timeRange.begin, timeRange.end);
        List<Long> totalDpv = flowMapper.dpvByDay(timeRange.begin, timeRange.end);
        List<Long> totalDuv = flowMapper.duvByDay(timeRange.begin, timeRange.end);
        List<Long> totalDau = flowMapper.allDauForUserIdByDay(timeRange.begin, timeRange.end);

        // 定义图表类型映射
        Map<String, String> chartTypes = new HashMap<>();
        chartTypes.put("dnu", "bar");  // 柱状图
        chartTypes.put("dau", "bar");  // 柱状图
        chartTypes.put("dpv", "line"); // 折线图
        chartTypes.put("duv", "line"); // 折线图

        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // 处理各类数据
        buildChartData("dnu 新注册用户数", totalDnu, series, yAxis, chartTypes.get("dnu"), "用户数",
                AxisVO.ofDateRange(timeRange.begin, timeRange.end));
        buildChartData("dau 日活跃用户数", totalDau, series, yAxis, chartTypes.get("dau"), "用户数",
                AxisVO.ofDateRange(timeRange.begin, timeRange.end));
        buildChartData("dpv 页面浏览量", totalDpv, series, yAxis, chartTypes.get("dpv"), "浏览量",
                AxisVO.ofDateRange(timeRange.begin, timeRange.end));
        buildChartData("duv 独立访客数", totalDuv, series, yAxis, chartTypes.get("duv"), "访客数",
                AxisVO.ofDateRange(timeRange.begin, timeRange.end));
        // x轴数据
        echartsVO.setXAxis(Collections.singletonList(AxisVO.ofDateRange(timeRange.begin, timeRange.end)));
        echartsVO.setYAxis(yAxis);
        echartsVO.setSeries(series);

        return echartsVO;
    }

    /**
     * 构建通用图表数据（包含空数据处理）
     */
    private void buildChartData(String name, List<Long> data, List<SerierVO> series, List<AxisVO> yAxis,
                                String chartType, String yAxisName, AxisVO xAxis) {

        // 处理空数据情况：确保数据长度与日期范围一致
        List<Long> validData = data != null ? data : new ArrayList<>();
        while (validData.size() < xAxis.getData().size()) {
            validData.add(0L);
        }

        List<Double> doubleData = validData.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());

        // 计算最大值和最小值（空数据时默认0）
        Double max = doubleData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double min = doubleData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // 构建系列数据（空数据时显示0或空点）
        series.add(new SerierVO(
                name,
                chartType,
                doubleData.isEmpty() ? Collections.singletonList(0.0) : doubleData,
                max + "次",
                min + "次"
        ));

        // 构建Y轴（空数据时保持合理区间）
        double effectiveMin = min == 0 ? 0 : min * 0.9;
        yAxis.add(AxisVO.builder()
                .max(max)
                .min(effectiveMin)
                .interval(calculateInterval(max, effectiveMin))
                .average(NumberUtils.setScale(NumberUtils.null2Zero(
                        doubleData.isEmpty() ? 0 : NumberUtils.average(doubleData)
                )))
                .type(AxisVO.TYPE_VALUE)
//            .name(yAxisName)
                .build());
    }


    @Override
    public EchartsVO urlVisits(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<String> visitData = flowMapper.top10UrlsByVisits(timeRange.begin, timeRange.end);
        Map<String, Long> urlVisitMap = parseUrlData(visitData);

        return buildUrlChart(urlVisitMap, "访问量", "url访问量前10", "bar");
    }

    @Override
    public EchartsVO urlErrors(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<String> errorData = flowMapper.top10UrlsByErrors(timeRange.begin, timeRange.end);
        Map<String, Long> urlErrorMap = parseUrlData(errorData);

        return buildUrlChart(urlErrorMap, "错误量", "url错误量前10", "bar");
    }

    /**
     * 解析URL统计数据
     */
    private Map<String, Long> parseUrlData(List<String> data) {
        Map<String, Long> result = new HashMap<>();
        if (data == null || data.isEmpty()) {
            return result;
        }

        for (String item : data) {
            String[] parts = item.split(",");
            if (parts.length >= 2) {
                String url = parts[0];
                Long count = Long.parseLong(parts[1]);
                result.put(url, count);
            }
        }
        return result;
    }

    /**
     * 构建URL统计图表
     */
    private EchartsVO buildUrlChart(Map<String, Long> dataMap, String yAxisName, String seriesName, String chartType) {
        if (dataMap.isEmpty()) {
            return new EchartsVO();
        }

        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // 提取X轴数据和Y轴数据
        List<String> xData = new ArrayList<>(dataMap.keySet());
        List<Double> yData = dataMap.values().stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());

        // 计算最大值和最小值
        Double max = yData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double min = yData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // 配置X轴（分类轴）
        AxisVO xAxis = AxisVO.builder()
                .type(AxisVO.TYPE_CATEGORY)
                .data(xData)
                .build();

        // 构建图表数据
        buildChartData(seriesName, yData.stream().mapToLong(Double::longValue).boxed().collect(Collectors.toList()),
                series, yAxis, chartType, yAxisName, xAxis);

        echartsVO.setXAxis(Collections.singletonList(xAxis));
        echartsVO.setYAxis(yAxis);
        echartsVO.setSeries(series);

        return echartsVO;
    }

    /**
     * 计算Y轴间隔
     */
    private double calculateInterval(double max, double min) {
        double range = max - min;
        return range <= 0 ? 1 : Math.ceil(range / 5);
    }

    /**
     * 获取时间范围（通用方法）
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