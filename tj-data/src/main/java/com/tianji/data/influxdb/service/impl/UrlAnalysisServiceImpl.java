package com.tianji.data.influxdb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.NumberUtils;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.domain.UrlMetrics;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.service.IUrlAnalysisService;
import com.tianji.data.influxdb.tool.UrlRegexConverter;
import com.tianji.data.model.query.UrlPageQuery;
import com.tianji.data.model.query.UrlQuery;
import com.tianji.data.model.vo.AxisVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.SerierVO;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.http.Url;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UrlAnalysisServiceImpl implements IUrlAnalysisService {
    
    private final BusinessLogMapper businessLogMapper;


    /**
     * 分析URL的访问指标
     * @return 包含访问指标的结果对象
     */
    public PageDTO<BusinessLog> analyzeUrl(UrlPageQuery query) {
        //示例 URL：/accounts/login
        // 调用Mapper方法执行查询
        String url = query.getUrl();
        String beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
        String endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        int pageNum = query.getPageNo();
        int pageSize = query.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<BusinessLog> list = businessLogMapper.findLogsByUrl(url, beginTime, endTime, pageSize, offset);
        // 统计总记录数
        Long total = businessLogMapper.countLogsByUrlToday(url, beginTime, endTime);

        // 创建 MyBatis-Plus 的 Page 对象
        Page<BusinessLog> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(list);

        // 将 IPage 转换为 PageDTO
        return PageDTO.of(page);
    }

    /**
     * 分析URL的访问指标(模糊搜索url)---注意SQL注入---
     * @return 包含访问指标的结果对象
     */
    public PageDTO<BusinessLog> analyzeUrlByLike(UrlPageQuery query) {
        //示例URL:/login

        // 将URL转换为InfluxDB正则表达式格式：/\/accounts/
        // 1. 转义URL中的斜杠：/login → \/login
        String escapedUrl = query.getUrl().replace("/", "\\\\/"); // 四个反斜杠表示一个\/

        // 2. 用/包裹整个正则：/\/login/
        String regex = "/" + escapedUrl + "/";

        String beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
        String endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        int pageNum = query.getPageNo();
        int pageSize = query.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<BusinessLog> list = businessLogMapper.findLogsByUrlByLike(regex, beginTime, endTime, pageSize, offset);
        // 统计总记录数
        Long total = businessLogMapper.countLogsByUrlTodayByLike(regex, beginTime, endTime);

        // 创建 MyBatis-Plus 的 Page 对象
        Page<BusinessLog> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(list);
        // 将 IPage 转换为 PageDTO
        return PageDTO.of(page);
    }

    @Override
    public EchartsVO getMetricByUrl(UrlQuery query) {
        String url = query.getUrl();
        String beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
        String endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);

        Long totalVisits = businessLogMapper.countTotalVisits(url, beginTime, endTime);
        Long failedVisits = businessLogMapper.countFailedVisits(url, beginTime, endTime);

        // 封装数据
        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();

        // 总访问量数据
        List<Double> totalVisitsData = Collections.singletonList(totalVisits.doubleValue());
        Double totalVisitsMax = totalVisits.doubleValue();
        Double totalVisitsMin = totalVisits.doubleValue();
        series.add(new SerierVO("总访问量", "value", totalVisitsData, totalVisitsMax + "次", totalVisitsMin + "次"));
        yAxis.add(AxisVO.builder()
                .max(totalVisitsMax)
                .min(totalVisitsMin * 0.9)
                .interval(((int) NumberUtils.div((totalVisitsMax - totalVisitsMin * 0.9), 10.0) + 1) * 1.0)
                .average(NumberUtils.setScale(NumberUtils.null2Zero(NumberUtils.average(totalVisitsData))))
                .type(AxisVO.TYPE_VALUE)
                .build());

        // 总报错量数据
        List<Double> failedVisitsData = Collections.singletonList(failedVisits.doubleValue());
        Double failedVisitsMax = failedVisits.doubleValue();
        Double failedVisitsMin = failedVisits.doubleValue();
        series.add(new SerierVO("总报错量", "value", failedVisitsData, failedVisitsMax + "次", failedVisitsMin + "次"));
        yAxis.add(AxisVO.builder()
                .max(failedVisitsMax)
                .min(failedVisitsMin * 0.9)
                .interval(((int) NumberUtils.div((failedVisitsMax - failedVisitsMin * 0.9), 10.0) + 1) * 1.0)
                .average(NumberUtils.setScale(NumberUtils.null2Zero(NumberUtils.average(failedVisitsData))))
                .type(AxisVO.TYPE_VALUE)
                .build());

        // x轴数据
        echartsVO.setXAxis(Collections.singletonList(AxisVO.last15Day()));
        // y轴数据
        echartsVO.setYAxis(yAxis);
        // series数据
        echartsVO.setSeries(series);

        return echartsVO;
    }
}