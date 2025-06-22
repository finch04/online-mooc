package com.tianji.data.influxdb.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.NumberUtils;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.mapper.FlowMapper;
import com.tianji.data.influxdb.service.IFlowService;
import com.tianji.data.influxdb.service.IUrlAnalysisService;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.AxisVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.SerierVO;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowServiceImpl implements IFlowService {
    
    private final FlowMapper flowMapper;

    @Override
    public EchartsVO dnu(FlowQuery query) {
        String beginTime = TimeHandlerUtils.getYesterdayTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();
        if(query.getBeginTime()!=null && query.getEndTime()!=null){
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }
        List<Long> totalDnu = flowMapper.dnuByDay(beginTime, endTime);
        // 封装数据
        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // ------------------- 修正总访问量数据映射 -------------------
        // 将Long数组转换为Double数组
        List<Double> totalVisitsData = totalDnu.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());
        // 计算实际最大值和最小值
        Double totalVisitsMax = totalVisitsData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double totalVisitsMin = totalVisitsData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        series.add(new SerierVO(
                "新注册用户数",
                "bar",
                totalVisitsData,
                totalVisitsMax + "次",
                totalVisitsMin + "次"
        ));
        yAxis.add(AxisVO.builder()
                .max(totalVisitsMax)
                .min(totalVisitsMin * 0.9)
                .interval(calculateInterval(totalVisitsMax, totalVisitsMin * 0.9))
                .average(NumberUtils.setScale(NumberUtils.null2Zero(NumberUtils.average(totalVisitsData))))
                .type(AxisVO.TYPE_VALUE)
                .build());

        // x轴数据
        echartsVO.setXAxis(Collections.singletonList(AxisVO.ofDateRange(beginTime, endTime)));
        // y轴数据
        echartsVO.setYAxis(yAxis);
        // series数据
        echartsVO.setSeries(series);

        return echartsVO;

    }

    @Override
    public EchartsVO dau(FlowQuery query) {
        return null;
    }

    @Override
    public EchartsVO dpv(FlowQuery query) {
        return null;
    }

    @Override
    public EchartsVO duv(FlowQuery query) {
        return null;
    }

    @Override
    public EchartsVO errorCount(FlowQuery query) {
        return null;
    }

    @Override
    public EchartsVO loginCount(FlowQuery query) {
        return null;
    }

    @Override
    public EchartsVO dnuBody(FlowQuery query) {
        return null;
    }

    // 计算Y轴间隔的辅助方法
    private double calculateInterval(double max, double min) {
        double range = max - min;
        if (range <= 0) return 1;
        return ((int) Math.ceil(range / 10.0) + 1) * 1.0;
    }
}