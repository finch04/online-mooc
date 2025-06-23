package com.tianji.data.service.impl;

import com.alibaba.fastjson.JSON;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.model.po.*;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianji.data.constants.RedisConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements IAnalysisService {

    private final ICourseConversionDpvService courseConversionDpvService;
    private final ICourseDetailGenderDuvService courseDetailGenderDuvService;
    private final ICourseDetailProvinceDuvService courseDetailProvinceDuvService;
    private final StringRedisTemplate redisTemplate;

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


    @Override
    public LogAnalysisResult analyzeLogs(List<BusinessLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return new LogAnalysisResult();
        }

        LogAnalysisResult result = new LogAnalysisResult();
        Map<String, UserProfile> userProfileMap = new HashMap<>();
        Map<String, Map<String, Long>> courseVisitBySex = new HashMap<>();
        Map<String, Map<String, Long>> courseVisitByProvince = new HashMap<>();

        for (BusinessLog log : logs) {
            // 检查关键字段是否为空，任一为空则跳过
            if (log.getUserId() == null ||
                    log.getUserName() == null ||
                    log.getSex() == null ||
                    log.getProvince() == null) {
                continue;
            }

            // 安全获取用户信息，不再设置默认值
            String userId = log.getUserId();
            String userName = log.getUserName();
            String sex = log.getSex();
            String province = log.getProvince();

            // 解析课程ID
            String courseId = extractCourseId(log.getRequestUri());
            if (courseId == null) {
                continue; // 无效的课程URL，跳过
            }

            // 更新用户画像
            userProfileMap.computeIfAbsent(userId, k -> {
                UserProfile profile = new UserProfile();
                profile.setUserId(userId);
                profile.setUserName(userName);
                profile.setSex(sex);
                profile.setProvince(province);
                profile.setCourseVisitCounts(new HashMap<>());
                return profile;
            });

            // 更新用户课程访问量
            userProfileMap.get(userId).getCourseVisitCounts()
                    .merge(courseId, 1L, Long::sum);

            // 更新性别维度统计
            courseVisitBySex.computeIfAbsent(sex, k -> new HashMap<>())
                    .merge(courseId, 1L, Long::sum);

            // 更新省份维度统计
            courseVisitByProvince.computeIfAbsent(province, k -> new HashMap<>())
                    .merge(courseId, 1L, Long::sum);
        }

        result.setUserProfiles(new ArrayList<>(userProfileMap.values()));
        result.setCourseVisitBySex(courseVisitBySex);
        result.setCourseVisitByProvince(courseVisitByProvince);

        cacheToRedis(result);
        return result;
    }

    /**
     * 将分析结果缓存到Redis
     */
    private void cacheToRedis(LogAnalysisResult result) {
        try {
            // 缓存用户画像数据
            List<UserProfile> userProfiles = result.getUserProfiles();
            for (UserProfile profile : userProfiles) {
                String redisKey = KEY_USER_PROFILE+profile.getUserId();
                String jsonValue = JSON.toJSONString(profile);
                redisTemplate.opsForValue().set(redisKey, jsonValue, 1, TimeUnit.DAYS);
            }

            // 缓存热门课程数据 - 性别维度
            Map<String, Map<String, Long>> courseVisitBySex = result.getCourseVisitBySex();
            for (Map.Entry<String, Map<String, Long>> sexEntry : courseVisitBySex.entrySet()) {
                String sex = sexEntry.getKey();
                Map<String, Long> courseCounts = sexEntry.getValue();

                String redisKey =KEY_HOT_COURSES_BY_SEX+sex;
                // 清空旧数据
                redisTemplate.delete(redisKey);
                // 存入新数据
                for (Map.Entry<String, Long> courseEntry : courseCounts.entrySet()) {
                    redisTemplate.opsForZSet().add(redisKey, courseEntry.getKey(), courseEntry.getValue());
                }
                // 设置过期时间
                redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            }

            // 缓存热门课程数据 - 省份维度
            Map<String, Map<String, Long>> courseVisitByProvince = result.getCourseVisitByProvince();
            for (Map.Entry<String, Map<String, Long>> provinceEntry : courseVisitByProvince.entrySet()) {
                String province = provinceEntry.getKey();
                Map<String, Long> courseCounts = provinceEntry.getValue();

                String redisKey = KEY_HOT_COURSES_BY_PROVINCE+province;
                // 清空旧数据
                redisTemplate.delete(redisKey);
                // 存入新数据
                for (Map.Entry<String, Long> courseEntry : courseCounts.entrySet()) {
                    redisTemplate.opsForZSet().add(redisKey, courseEntry.getKey(), courseEntry.getValue());
                }
                // 设置过期时间
                redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            }

            log.info("日志分析结果已缓存到Redis");
        } catch (Exception e) {
            log.error("缓存分析结果到Redis失败", e);
        }
    }

    /**
     * 从请求URI中提取课程ID
     * 示例：/cs/courses/baseInfo/8 -> 8
     */
    private String extractCourseId(String requestUri) {
        if (requestUri == null || !requestUri.startsWith("/cs/courses/baseInfo/")) {
            return null;
        }

        try {
            // 提取最后一个斜杠后的部分作为课程ID
            int lastSlashIndex = requestUri.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < requestUri.length() - 1) {
                return requestUri.substring(lastSlashIndex + 1);
            }
        } catch (Exception e) {
            // 记录异常但不中断处理
            System.err.println("Failed to extract course ID from URI: " + requestUri);
        }

        return null;
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