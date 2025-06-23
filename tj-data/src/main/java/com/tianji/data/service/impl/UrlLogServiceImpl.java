package com.tianji.data.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.NumberUtils;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.mapper.BusinessLogMapper;
import com.tianji.data.service.IUrlLogService;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.AxisVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.SerierVO;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlLogServiceImpl implements IUrlLogService {
    
    private final BusinessLogMapper businessLogMapper;


    /**
     * 分析URL的访问指标
     * @return 包含访问指标的结果对象
     */
    public PageDTO<BusinessLog> getLogsPageByUrl(UrlPageQuery query) {
        if(query.getUrl()==null){
            throw new BizIllegalException("请输入URL");
        }
        //示例 URL：/accounts/login
        // 调用Mapper方法执行查询
        String url = query.getUrl();

        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();
        if(query.getBeginTime()!=null && query.getEndTime()!=null){
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }

        int pageNum = query.getPageNo();
        int pageSize = query.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<BusinessLog> list = null;
        Long total = null;
        try {
            list = businessLogMapper.findLogsByUrl(url, beginTime, endTime, pageSize, offset);
            // 统计总记录数
            total = businessLogMapper.countLogsByUrlToday(url, beginTime, endTime);
        } catch (Exception e) {
            log.info("influxdb搜索异常",e);
            throw new BizIllegalException("搜索url不合法");
        }

        // 创建 MyBatis-Plus 的 Page 对象
        Page<BusinessLog> page = new Page<>(pageNum, pageSize, total);
        // 或者保持 Stream 链式调用（但返回的列表元素与原列表相同）
        List<BusinessLog> result = list.stream()
                .peek(i -> i.setTime(TimeHandlerUtils.formatIsoTime(i.getTime())))
                .collect(Collectors.toList());
        page.setRecords(result);

        // 将 IPage 转换为 PageDTO
        return PageDTO.of(page);
    }

    /**
     * 分析URL的访问指标(模糊搜索url)---注意SQL注入---
     * @return 包含访问指标的结果对象
     */
    public PageDTO<BusinessLog> getLogsPageByUrlByLike(UrlPageQuery query) {
        //示例URL:/login

        // 将URL转换为InfluxDB正则表达式格式：/\/accounts/
        // 1. 转义URL中的斜杠：/login → \/login
        String escapedUrl = query.getUrl().replace("/", "\\\\/"); // 四个反斜杠表示一个\/

        // 2. 用/包裹整个正则：/\/login/
        String regex = "/" + escapedUrl + "/";

        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();
        if(query.getBeginTime()!=null && query.getEndTime()!=null){
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }

        int pageNum = query.getPageNo();
        int pageSize = query.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<BusinessLog> list = null;
        Long total = null;
        try {
            list = businessLogMapper.findLogsByUrlByLike(regex, beginTime, endTime, pageSize, offset);
            // 统计总记录数
            total = businessLogMapper.countLogsByUrlTodayByLike(regex, beginTime, endTime);
        } catch (Exception e) {
            log.info("influxdb搜索异常",e);
            throw new BizIllegalException("搜索url不合法");
        }

        // 创建 MyBatis-Plus 的 Page 对象
        Page<BusinessLog> page = new Page<>(pageNum, pageSize, total);
        // 或者保持 Stream 链式调用（但返回的列表元素与原列表相同）
        List<BusinessLog> result = list.stream()
                .peek(i -> i.setTime(TimeHandlerUtils.formatIsoTime(i.getTime())))
                .collect(Collectors.toList());
        page.setRecords(result);
        // 将 IPage 转换为 PageDTO
        return PageDTO.of(page);
    }

    @Override
    public EchartsVO getMetricByUrl(UrlQuery query) {
        String url = query.getUrl();

        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();
        if (query.getBeginTime() != null && query.getEndTime() != null) {
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }

        // 假设这两个方法已返回每日统计数组（例如7天数据对应7个元素）
        List<Long> totalVisits = null;
        List<Long> failedVisits = null;
        try {
            totalVisits = businessLogMapper.countDailyVisits(url, beginTime, endTime);
            failedVisits = businessLogMapper.countFailedVisits(url, beginTime, endTime);
        } catch (Exception e) {
            log.info("influxdb搜索异常",e);
            throw new BizIllegalException("搜索url不合法");
        }

        // 封装数据
        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // ------------------- 修正总访问量数据映射 -------------------
        // 将Long数组转换为Double数组
        List<Double> totalVisitsData = totalVisits.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());
        // 计算实际最大值和最小值
        Double totalVisitsMax = totalVisitsData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double totalVisitsMin = totalVisitsData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        series.add(new SerierVO(
                "访问量",
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

        // ------------------- 修正总报错量数据映射 -------------------
        List<Double> failedVisitsData = failedVisits.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());
        Double failedVisitsMax = failedVisitsData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double failedVisitsMin = failedVisitsData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        series.add(new SerierVO(
                "报错量",
                "line",
                failedVisitsData,
                failedVisitsMax + "次",
                failedVisitsMin + "次"
        ));
        yAxis.add(AxisVO.builder()
                .max(failedVisitsMax)
                .min(failedVisitsMin * 0.9)
                .interval(calculateInterval(failedVisitsMax, failedVisitsMin * 0.9))
                .average(NumberUtils.setScale(NumberUtils.null2Zero(NumberUtils.average(failedVisitsData))))
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
    public EchartsVO getMetricByUrlByLike(UrlQuery query) {
        String url = query.getUrl();
        // 将URL转换为InfluxDB正则表达式格式：/\/accounts/
        // 1. 转义URL中的斜杠：/login → \/login
        String escapedUrl = query.getUrl().replace("/", "\\\\/"); // 四个反斜杠表示一个\/

        // 2. 用/包裹整个正则：/\/login/
        String regex = "/" + escapedUrl + "/";

        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();
        if (query.getBeginTime() != null && query.getEndTime() != null) {
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }

        // 假设这两个方法已返回每日统计数组（例如7天数据对应7个元素）
        List<Long> totalVisits = null;
        List<Long> failedVisits = null;
        try {
            totalVisits = businessLogMapper.countDailyVisitsByLike(regex, beginTime, endTime);
            failedVisits = businessLogMapper.countFailedVisitsByLike(regex, beginTime, endTime);
        } catch (Exception e) {
            log.info("influxdb搜索异常",e);
            throw new BizIllegalException("搜索url不合法");
        }

        // 封装数据
        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // ------------------- 修正总访问量数据映射 -------------------
        // 将Long数组转换为Double数组
        List<Double> totalVisitsData = totalVisits.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());
        // 计算实际最大值和最小值
        Double totalVisitsMax = totalVisitsData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double totalVisitsMin = totalVisitsData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        series.add(new SerierVO(
                "访问量",
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

        // ------------------- 修正总报错量数据映射 -------------------
        List<Double> failedVisitsData = failedVisits.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());
        Double failedVisitsMax = failedVisitsData.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Double failedVisitsMin = failedVisitsData.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        series.add(new SerierVO(
                "报错量",
                "line",
                failedVisitsData,
                failedVisitsMax + "次",
                failedVisitsMin + "次"
        ));
        yAxis.add(AxisVO.builder()
                .max(failedVisitsMax)
                .min(failedVisitsMin * 0.9)
                .interval(calculateInterval(failedVisitsMax, failedVisitsMin * 0.9))
                .average(NumberUtils.setScale(NumberUtils.null2Zero(NumberUtils.average(failedVisitsData))))
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

    // 计算Y轴间隔的辅助方法
    private double calculateInterval(double max, double min) {
        double range = max - min;
        if (range <= 0) return 1;
        return ((int) Math.ceil(range / 10.0) + 1) * 1.0;
    }


    @Override
    public void exportLogs(HttpServletResponse response) throws IOException {
        // 设置Excel文件的MIME类型
        response.setHeader("Content-Disposition", "attachment; filename=export.xlsx");
        response.setHeader("X-Skip-Logging", "true"); // 设置跳过日志的标识
        // 响应类型,编码
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");

        //默认是导出7天内日志
        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();

        // 写入Excel数据
        try (OutputStream out = response.getOutputStream()) {
            EasyExcel.write(out, BusinessLog.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("日志数据")
                    .doWrite(
                            businessLogMapper.getAllLogsByTime(beginTime, endTime).stream()
                                    .peek(i -> i.setTime(TimeHandlerUtils.formatIsoTime(i.getTime())))
                                    .collect(Collectors.toList())
                    );
            // 确保数据全部写出
            out.flush();
        } catch (Exception e) {
            // 错误处理
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败");
        }
    }
}