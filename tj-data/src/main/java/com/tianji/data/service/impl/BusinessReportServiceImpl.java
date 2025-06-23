package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.mapper.*;
import com.tianji.data.model.po.*;
import com.tianji.data.service.*;
import com.tianji.data.utils.TimeHandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName BusinessReportServiceImpl.java
 * @Description 日志持久化到MySQL接口
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessReportServiceImpl implements IBusinessReportService {

    private final DuvMapper duvMapper;
    private final DnuMapper dnuMapper;
    private final DpvMapper dpvMapper;
    private final DauMapper dauMapper;
    private final DauRangeMapper dauRangeMapper;
    private final DauProvinceMapper dauProvinceMapper;
    private final CourseConversionDpvMapper courseConversionDpvMapper;
    private final CourseDetailGenderDuvMapper courseDetailGenderDuvMapper;
    private final CourseDetailProvinceDuvMapper courseDetailProvinceDuvMapper;
    private final DpvTimeMapper dpvTimeMapper;

    private final BusinessLogMapper businessLogMapper;
    private final FlowMapper flowMapper;

    @Override
    @Transactional
    public void saveLogs(List<BusinessLog> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 获取统计日期（当天）
        LocalDate reportDate = LocalDate.now();
        LocalDateTime reportDateTime = reportDate.atStartOfDay();

        String begin = reportDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toString();
        String end = reportDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toString();

        // 独立检查并统计每个表的数据
        saveDuvStatistics(list, reportDate);
        saveDnuStatistics(begin, end, reportDate);
        saveDpvStatistics(list, reportDate);
        saveDauStatistics(begin, end, reportDate);
        saveDauRangeStatistics(begin, end, reportDate);
        saveDauProvinceStatistics(list, reportDate);
        saveCourseAccessStatistics(begin, end, reportDate);//这里统计课程性别、省市、转化率
        saveDpvTimeStatistics(list, reportDateTime);
    }

    /**
     * 统计日唯一访问量（DUV）
     */
    private void saveDuvStatistics(List<BusinessLog> list, LocalDate reportDate) {
        // 直接使用Mapper检查Duv表是否已有当天数据
        if (existsDuvByReportTime(reportDate)) {
            log.info("Duv statistics already exist for date: {}", reportDate);
            return;
        }

        Set<String> uniqueUserIds = new HashSet<>();
        for (BusinessLog log : list) {
            if (log.getUserId() != null) {
                uniqueUserIds.add(log.getUserId());
            }
        }
        Duv duv = new Duv();
        duv.setDuv((long) uniqueUserIds.size());
        duv.setReportTime(reportDate);
        duvMapper.insert(duv);
        log.info("Saved Duv: {}", duv);
    }

    /**
     * 统计日新增用户数（DNU）
     */
    private void saveDnuStatistics(String begin, String end, LocalDate reportDate) {
        // 直接使用Mapper检查Dnu表是否已有当天数据
        if (existsDnuByReportTime(reportDate)) {
            log.info("Dnu statistics already exist for date: {}", reportDate);
            return;
        }

        List<String> newUserIds = flowMapper.newDnuForResponseBody(begin, end);
        Set<String> newUserSet = new HashSet<>(newUserIds);
        Dnu dnu = new Dnu();
        dnu.setDnu((long) newUserSet.size());
        dnu.setReportTime(reportDate);
        dnuMapper.insert(dnu);
        log.info("Saved Dnu: {}", dnu);
    }

    /**
     * 统计日访问页面量（DPV）
     */
    private void saveDpvStatistics(List<BusinessLog> list, LocalDate reportDate) {
        // 直接使用Mapper检查Dpv表是否已有当天数据
        if (existsDpvByReportTime(reportDate)) {
            log.info("Dpv statistics already exist for date: {}", reportDate);
            return;
        }

        Dpv dpv = new Dpv();
        dpv.setDpv((long) list.size());
        dpv.setReportTime(reportDate);
        dpvMapper.insert(dpv);
        log.info("Saved Dpv: {}", dpv);
    }

    /**
     * 统计用户日活跃数（DAU）
     */
    private void saveDauStatistics(String begin, String end, LocalDate reportDate) {
        // 直接使用Mapper检查Dau表是否已有当天数据
        if (existsDauByReportTime(reportDate)) {
            log.info("Dau statistics already exist for date: {}", reportDate);
            return;
        }

        Set<String> allActiveUserIds = new HashSet<>(flowMapper.allDauForUserId(begin, end));
        Set<String> newUserIds = new HashSet<>(flowMapper.newDnuForResponseBody(begin, end));
        Set<String> oldUserIds = new HashSet<>(allActiveUserIds);
        oldUserIds.removeAll(newUserIds);

        Dau dau = new Dau();
        dau.setAllDau((long) allActiveUserIds.size());
        dau.setNewDau((long) newUserIds.size());
        dau.setOldDau((long) oldUserIds.size());
        dau.setReportTime(reportDate);
        dauMapper.insert(dau);
        log.info("Saved Dau: {}", dau);
    }

    /**
     * 统计用户日活跃数范围（DAU Range）
     */
    private void saveDauRangeStatistics(String begin, String end, LocalDate reportDate) {
        // 直接使用Mapper检查DauRange表是否已有当天数据
        if (existsDauRangeByReportTime(reportDate)) {
            log.info("DauRange statistics already exist for date: {}", reportDate);
            return;
        }

        Set<String> allActiveUserIds = new HashSet<>(flowMapper.allDauForUserId(begin, end));
        String dauRangeStr;
        long userNum = allActiveUserIds.size();

        if (userNum <= 100) {
            dauRangeStr = "1-100人";
        } else if (userNum <= 500) {
            dauRangeStr = "101-500人";
        } else {
            dauRangeStr = "500人以上";
        }

        DauRange dauRange = new DauRange();
        dauRange.setDauRang(dauRangeStr);
        dauRange.setUserNum(userNum);
        dauRange.setReportTime(reportDate);
        dauRangeMapper.insert(dauRange);
        log.info("Saved DauRange: {}", dauRange);
    }


    /**
     * 统计活跃用户所属省份（DAU Province）
     */
    private void saveDauProvinceStatistics(List<BusinessLog> list, LocalDate reportDate) {

        String begin = TimeHandlerUtils.getYesterdayTime().getBegin();
        String end = TimeHandlerUtils.getYesterdayTime().getEnd();
        Set<String> activeUserIds = new HashSet<>(flowMapper.allDauForUserId(begin, end));
        // 统计每个省份的活跃用户数
        Map<String, Integer> provinceDauMap = new HashMap<>();
        for (BusinessLog log : list) {
            if (activeUserIds.contains(log.getUserId()) && log.getProvince() != null) {
                provinceDauMap.put(log.getProvince(), provinceDauMap.getOrDefault(log.getProvince(), 0) + 1);
            }
        }

        // 保存统计结果
        for (Map.Entry<String, Integer> entry : provinceDauMap.entrySet()) {
            // 直接使用Mapper检查该省份是否已有记录
            if (!existsDauProvinceByProvinceAndReportTime(entry.getKey(), reportDate)) {
                DauProvince dauProvince = new DauProvince();
                dauProvince.setDau((long) entry.getValue());
                dauProvince.setProvince(entry.getKey());
                dauProvince.setReportTime(reportDate);
                dauProvinceMapper.insert(dauProvince);
                log.info("Saved DauProvince: {}", dauProvince);
            } else {
                log.info("DauProvince already exists for province: {}, reportTime: {}", entry.getKey(), reportDate);
            }
        }
    }

    /**
     * 统计课程访问相关数据
     */
    private void saveCourseAccessStatistics(String begin, String end, LocalDate reportDate) {
        // 统计 购买课程url访问日志
        List<BusinessLog> buyCourseList = flowMapper.buyCourseList(begin, end);
        long buyCourseDpv = buyCourseList.size();

        // 统计 课程详情url访问日志
        List<BusinessLog> courseDetailList = flowMapper.courseDetailList(begin, end);
        long courseDetailDpv = courseDetailList.size();

        // 计算转化率
        double conversionRate = courseDetailDpv == 0 ? 0 : (double) buyCourseDpv / courseDetailDpv;

        // 保存转化率数据（直接使用Mapper检查是否已有记录）
        CourseConversionDpv courseConversionDpv = new CourseConversionDpv();
        courseConversionDpv.setDoBrowseDpv(courseDetailDpv); // 设置浏览量，确保不为null
        courseConversionDpv.setDoOrderDpv(buyCourseDpv);    // 设置下单量，确保不为null
        courseConversionDpv.setConversionRate(conversionRate);
        courseConversionDpv.setReportTime(reportDate);

        if (!existsCourseConversionDpvByReportTime(reportDate)) {
            courseConversionDpvMapper.insert(courseConversionDpv);
            log.info("Saved CourseConversionDpv: {}", courseConversionDpv);
        } else {
            log.info("CourseConversionDpv statistics already exist for date: {}", reportDate);
        }

        // 统计课程详情按性别访问数
        Map<String, Long> genderDuvMap = new HashMap<>();
        genderDuvMap.put("man", 0L);
        genderDuvMap.put("woman", 0L);

        for (BusinessLog log : courseDetailList) {
            if ("男".equals(log.getSex())) {
                genderDuvMap.put("man", genderDuvMap.get("man") + 1);
            } else if ("女".equals(log.getSex())) {
                genderDuvMap.put("woman", genderDuvMap.get("woman") + 1);
            }
        }

        // 保存性别访问数数据（直接使用Mapper检查是否已有记录）
        CourseDetailGenderDuv courseDetailGenderDuv = new CourseDetailGenderDuv();
        courseDetailGenderDuv.setManDpv(genderDuvMap.get("man")); // 确保不为null
        courseDetailGenderDuv.setWomanDpv(genderDuvMap.get("woman")); // 确保不为null
        courseDetailGenderDuv.setReportTime(reportDate);

        if (!existsCourseDetailGenderDuvByReportTime(reportDate)) {
            courseDetailGenderDuvMapper.insert(courseDetailGenderDuv);
            log.info("Saved CourseDetailGenderDuv: {}", courseDetailGenderDuv);
        } else {
            log.info("CourseDetailGenderDuv statistics already exist for date: {}", reportDate);
        }

        // 统计课程详情按省份访问数
        Map<String, Long> provinceDuvMap = new HashMap<>();
        for (BusinessLog log : courseDetailList) {
            if (log.getProvince() != null) {
                provinceDuvMap.put(log.getProvince(), provinceDuvMap.getOrDefault(log.getProvince(), 0L) + 1);
            }
        }

        // 保存省份访问数数据（直接使用Mapper检查是否已有记录）
        for (Map.Entry<String, Long> entry : provinceDuvMap.entrySet()) {
            CourseDetailProvinceDuv courseDetailProvinceDuv = new CourseDetailProvinceDuv();
            courseDetailProvinceDuv.setProvinceName(entry.getKey());
            courseDetailProvinceDuv.setDuv(entry.getValue()); // 确保不为null
            courseDetailProvinceDuv.setReportTime(reportDate);

            if (!existsCourseDetailProvinceDuvByProvinceNameAndReportTime(entry.getKey(), reportDate)) {
                courseDetailProvinceDuvMapper.insert(courseDetailProvinceDuv);
                log.info("Saved CourseDetailProvinceDuv: {}", courseDetailProvinceDuv);
            } else {
                log.info("CourseDetailProvinceDuv statistics already exist for date: {}", reportDate);
            }
        }
    }

    /**
     * 统计用户活跃访问数时段分布（DpvTime）
     */
    private void saveDpvTimeStatistics(List<BusinessLog> list, LocalDateTime reportDateTime) {
        // 直接使用Mapper检查DpvTime表是否已有当天数据
        if (existsDpvTimeByReportTime(reportDateTime)) {
            log.info("DpvTime statistics already exist for date: {}", reportDateTime.toLocalDate());
            return;
        }

        // 初始化各时段访问量（基于系统默认时区）
        long midNightDpv = 0;  // 系统时区 0~7点
        long noonDpv = 0;      // 系统时区 7~12点
        long afternoonDpv = 0; // 系统时区 12~18点
        long eveningDpv = 0;   // 系统时区 18~24点

        // 获取系统默认时区
        ZoneId systemZone = ZoneId.systemDefault();

        // 遍历日志，按系统时区小时统计访问量
        for (BusinessLog log : list) {
            Instant instant = Instant.parse(log.getTime());
            // 转换为系统时区的ZonedDateTime
            ZonedDateTime zonedDateTime = instant.atZone(systemZone);
            int localHour = zonedDateTime.getHour();

            if (localHour >= 0 && localHour < 7) {
                midNightDpv++;
            } else if (localHour >= 7 && localHour < 12) {
                noonDpv++;
            } else if (localHour >= 12 && localHour < 18) {
                afternoonDpv++;
            } else {
                eveningDpv++;
            }
        }
        // 创建并保存DpvTime对象
        DpvTime dpvTime = new DpvTime();
        dpvTime.setReportTime(reportDateTime);
        dpvTime.setMidNightDpv(midNightDpv);
        dpvTime.setNoonDpv(noonDpv);
        dpvTime.setAfternoonDpv(afternoonDpv);
        dpvTime.setEveningDpv(eveningDpv);
        dpvTimeMapper.insert(dpvTime);
        log.info("Saved DpvTime statistics for date: {}", reportDateTime.toLocalDate());
    }

    // 以下是使用MyBatis-Plus Mapper直接查询的辅助方法
    private boolean existsDuvByReportTime(LocalDate reportTime) {
        return duvMapper.selectCount(new LambdaQueryWrapper<Duv>()
                .eq(Duv::getReportTime, reportTime)) > 0;
    }

    private boolean existsDnuByReportTime(LocalDate reportTime) {
        return dnuMapper.selectCount(new LambdaQueryWrapper<Dnu>()
                .eq(Dnu::getReportTime, reportTime)) > 0;
    }

    private boolean existsDpvByReportTime(LocalDate reportTime) {
        return dpvMapper.selectCount(new LambdaQueryWrapper<Dpv>()
                .eq(Dpv::getReportTime, reportTime)) > 0;
    }

    private boolean existsDauByReportTime(LocalDate reportTime) {
        return dauMapper.selectCount(new LambdaQueryWrapper<Dau>()
                .eq(Dau::getReportTime, reportTime)) > 0;
    }

    private boolean existsDauRangeByReportTime(LocalDate reportTime) {
        return dauRangeMapper.selectCount(new LambdaQueryWrapper<DauRange>()
                .eq(DauRange::getReportTime, reportTime)) > 0;
    }


    private boolean existsDauProvinceByProvinceAndReportTime(String province, LocalDate reportTime) {
        return dauProvinceMapper.selectCount(new LambdaQueryWrapper<DauProvince>()
                .eq(DauProvince::getProvince, province)
                .eq(DauProvince::getReportTime, reportTime)) > 0;
    }

    private boolean existsCourseConversionDpvByReportTime(LocalDate reportTime) {
        return courseConversionDpvMapper.selectCount(new LambdaQueryWrapper<CourseConversionDpv>()
                .eq(CourseConversionDpv::getReportTime, reportTime)) > 0;
    }

    private boolean existsCourseDetailGenderDuvByReportTime(LocalDate reportTime) {
        return courseDetailGenderDuvMapper.selectCount(new LambdaQueryWrapper<CourseDetailGenderDuv>()
                .eq(CourseDetailGenderDuv::getReportTime, reportTime)) > 0;
    }

    private boolean existsCourseDetailProvinceDuvByProvinceNameAndReportTime(String provinceName, LocalDate reportTime) {
        return courseDetailProvinceDuvMapper.selectCount(new LambdaQueryWrapper<CourseDetailProvinceDuv>()
                .eq(CourseDetailProvinceDuv::getProvinceName, provinceName)
                .eq(CourseDetailProvinceDuv::getReportTime, reportTime)) > 0;
    }

    private boolean existsDpvTimeByReportTime(LocalDateTime reportTime) {
        return dpvTimeMapper.selectCount(new LambdaQueryWrapper<DpvTime>()
                .eq(DpvTime::getReportTime, reportTime)) > 0;
    }
}