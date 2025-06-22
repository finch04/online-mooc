package com.tianji.data.utils;

import com.google.common.collect.Lists;
import com.tianji.common.utils.ExceptionsUtil;
import com.tianji.data.influxdb.domain.dto.TimeDTO;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * YestordayUtils
 *
 * @author: wgl
 * @describe: TODO
 * @date: 2022/12/28 10:10
 */
@Slf4j
public class TimeHandlerUtils {

    // 定义默认的日期时间格式
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static String formatIsoTime(String isoTime) {
        return formatIsoTime(isoTime, "Asia/Shanghai", "yyyy-MM-dd HH:mm:ss");
    }

    // 将ISO 8601时间字符串转换为本地时间字符串
    public static String formatIsoTime(String isoTime, String zoneId, String formatPattern) {
        if (isoTime == null) {
            return "";
        }
        try {
            // 解析ISO时间字符串为Instant（UTC时间）
            Instant instant = Instant.parse(isoTime);
            // 转换为指定时区的LocalDateTime
            LocalDateTime localDateTime = localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of(zoneId));
            // 格式化为指定格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            return isoTime; // 转换失败时返回原始字符串
        }
    }

    /**
     * 计算两个日期字符串之间的天数
     * @param startDateStr 开始日期字符串 (格式: yyyy-MM-dd)
     * @param endDateStr 结束日期字符串 (格式: yyyy-MM-dd)
     * @return 天数差
     */
    public static int getDaysBetween(String startDateStr, String endDateStr) {
        if (startDateStr == null || endDateStr == null) {
            return 0;
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DEFAULT_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DEFAULT_FORMATTER);

            // 计算两个日期之间的天数差（包含结束日期）
            return (int) startDate.until(endDate, java.time.temporal.ChronoUnit.DAYS) + 1;
        } catch (DateTimeParseException e) {
            // 日期格式解析失败时返回0
            return 0;
        }
    }

    /**
     * 将 LocalDateTime 转换为字符串
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @param formatter 日期时间格式化器，如果为 null 则使用默认格式化器
     * @return 格式化后的字符串
     */
    public static String localDateTimeToString(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        if (localDateTime == null) {
            return null;
        }
        if (formatter == null) {
            formatter = DEFAULT_FORMATTER;
        }
        return localDateTime.format(formatter);
    }

    /**
     * 将字符串转换为 LocalDateTime
     * @param timeStr 要转换的字符串
     * @param formatter 日期时间格式化器，如果为 null 则使用默认格式化器
     * @return 转换后的 LocalDateTime 对象
     */
    public static LocalDateTime stringToLocalDateTime(String timeStr, DateTimeFormatter formatter) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        if (formatter == null) {
            formatter = DEFAULT_FORMATTER;
        }
        return LocalDateTime.parse(timeStr, formatter);
    }

    /**
     * 获取7天前的时间段
     *
     * @return 包含7天前日期范围的TimeDTO对象
     */
    public static TimeDTO getSevenDaysAgoTime() {
        try {
            TimeDTO timeDTO = new TimeDTO();
            // 获取7天前的日期
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6L);
            LocalDateTime maxTime = LocalDateTime.of(sevenDaysAgo.toLocalDate(), LocalTime.MAX);
            LocalDateTime minTime = LocalDateTime.of(sevenDaysAgo.toLocalDate(), LocalTime.MIN);

            // 设置开始时间和结束时间
            timeDTO.setBeginTime(minTime);
            timeDTO.setEndTime(maxTime);

            // 封装字符串格式的开始时间和结束时间
            timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
            timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));

            // 设置目标日期（7天前的日期）
            timeDTO.setTargetDate(sevenDaysAgo.toLocalDate());
            return timeDTO;
        } catch (Exception e) {
            log.error("7天前日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("7天前日期数据获取失败");
        }
    }

    /**
     * 获取昨日时间段
     *
     * @return
     */
    public static TimeDTO getYesterdayTime() {
        try {
            TimeDTO timeDTO = new TimeDTO();
            LocalDateTime now = LocalDateTime.now().plusDays(-1L);
            LocalDateTime maxTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
            LocalDateTime minTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
            //设置开始时间
            timeDTO.setBeginTime(minTime);
            //设置结束时间
            timeDTO.setEndTime(maxTime);
            //封装开始时间字符串
            timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
            //封装结束时间字符串
            timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));
            timeDTO.setTargetDate(LocalDate.now());
            return timeDTO;
        } catch (Exception e) {
            log.error("日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("昨日日期数据获取失败");
        }
    }

    /**
     * 获取昨日起始到结束时间段
     *
     * @return
     */
    public static TimeDTO getYesterdayTime(String reportTime) {
        try {
            reportTime = reportTime+"T00:00:00";
            TimeDTO timeDTO = new TimeDTO();
            LocalDateTime targetDateTime = LocalDateTime.parse(reportTime).plusDays(-1L);
            LocalDateTime maxTime = LocalDateTime.of(targetDateTime.toLocalDate(), LocalTime.MAX);
            LocalDateTime minTime = LocalDateTime.of(targetDateTime.toLocalDate(), LocalTime.MIN);
            //设置开始时间
            timeDTO.setBeginTime(minTime);
            //设置结束时间
            timeDTO.setEndTime(maxTime);
            //封装开始时间字符串
            timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
            //封装结束时间字符串
            timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));
            timeDTO.setTargetDate(targetDateTime.toLocalDate());
            return timeDTO;
        } catch (Exception e) {
            log.error("日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("昨日日期数据获取失败");
        }
    }

    /**
     * 获取今天起始到结束时间段（无参版本）
     *
     * @return TimeDTO 包含今日时间范围的对象
     */
    public static TimeDTO getTodayTime() {
        try {
            TimeDTO timeDTO = new TimeDTO();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime maxTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
            LocalDateTime minTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);

            // 设置开始时间
            timeDTO.setBeginTime(minTime);
            // 设置结束时间
            timeDTO.setEndTime(maxTime);
            // 封装开始时间字符串
            timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
            // 封装结束时间字符串
            timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));
            timeDTO.setTargetDate(LocalDate.now());

            return timeDTO;
        } catch (Exception e) {
            log.error("日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("今日日期数据获取失败");
        }
    }

    /**
     * 获取今天起始到结束时间段
     *
     * @return
     */
    public static TimeDTO getTodayTime(String reportTime) {
        try {
            reportTime = reportTime+"T00:00:00";
            TimeDTO timeDTO = new TimeDTO();
            LocalDateTime targetDateTime = LocalDateTime.parse(reportTime);
            LocalDateTime maxTime = LocalDateTime.of(targetDateTime.toLocalDate(), LocalTime.MAX);
            LocalDateTime minTime = LocalDateTime.of(targetDateTime.toLocalDate(), LocalTime.MIN);
            //设置开始时间
            timeDTO.setBeginTime(minTime);
            //设置结束时间
            timeDTO.setEndTime(maxTime);
            //封装开始时间字符串
            timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
            //封装结束时间字符串
            timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));
            timeDTO.setTargetDate(targetDateTime.toLocalDate());
            return timeDTO;
        } catch (Exception e) {
            log.error("日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("昨日日期数据获取失败");
        }
    }


    /**
     * 获取昨日起始到结束24个时间段
     *
     * @return
     */
    public static List<TimeDTO> getYesterday24Time(String reportTime) {
        try {
            reportTime = reportTime+"T00:00:00";
            List<TimeDTO> timeDTOs = Lists.newArrayList();
            for (int i = 0; i <= 23; i++) {
                TimeDTO timeDTO = new TimeDTO();
                LocalDateTime targetDateTime = LocalDateTime.parse(reportTime).plusDays(-1).plusHours(i);
                LocalDateTime minTime = targetDateTime.withMinute(0).withSecond(0).withNano(0);
                LocalDateTime maxTime = minTime.plusHours(1).minusNanos(1);
                //设置开始时间
                timeDTO.setBeginTime(minTime);
                //设置结束时间
                timeDTO.setEndTime(maxTime);
                //封装开始时间字符串
                timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
                //封装结束时间字符串
                timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));
                timeDTO.setTargetDateTime(targetDateTime);
                timeDTOs.add(timeDTO);
            }

            return timeDTOs;
        } catch (Exception e) {
            log.error("日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("昨日日期数据获取失败");
        }
    }

    /**
     * 获取今天起始到结束24个时间段
     *
     * @return
     */
    public static List<TimeDTO> getToday24Time(String reportTime) {
        try {
            reportTime = reportTime+"T00:00:00";
            List<TimeDTO> timeDTOs = Lists.newArrayList();
            for (int i = 0; i <= 23; i++) {
                TimeDTO timeDTO = new TimeDTO();
                LocalDateTime targetDateTime = LocalDateTime.parse(reportTime).plusHours(i);
                LocalDateTime minTime = targetDateTime.withMinute(0).withSecond(0).withNano(0);
                LocalDateTime maxTime = minTime.plusHours(1).minusNanos(1);
                //设置开始时间
                timeDTO.setBeginTime(minTime);
                //设置结束时间
                timeDTO.setEndTime(maxTime);
                //封装开始时间字符串
                timeDTO.setBegin(timeDTO.getTimeFormatter().format(minTime));
                //封装结束时间字符串
                timeDTO.setEnd(timeDTO.getTimeFormatter().format(maxTime));
                timeDTO.setTargetDateTime(targetDateTime);
                timeDTOs.add(timeDTO);
            }

            return timeDTOs;
        } catch (Exception e) {
            log.error("日期计算异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new RuntimeException("昨日日期数据获取失败");
        }
    }

    /**
     * 根据时间单位获取其中的每一个时间节点
     * @param beginTime 起始时间
     * @param endTime 结束时间
     * @param timeUnit 时间单位
     * @return List<String> 包含时间节点的列表
     */
    public static List<String> getGraduallys(String beginTime, String endTime, TimeUnit timeUnit) {
        List<String> timeNodes = new ArrayList<>();
        SimpleDateFormat dateFormat = getDateFormat(beginTime);
        try {
            Date start = dateFormat.parse(beginTime);
            Date end = dateFormat.parse(endTime);
            long duration = end.getTime() - start.getTime();
            switch (timeUnit) {
                case DAYS:
                    long days = TimeUnit.MILLISECONDS.toDays(duration);
                    for (long i = 0; i <= days; i++) {
                        Date node = new Date(start.getTime() + TimeUnit.DAYS.toMillis(i));
                        timeNodes.add(dateFormat.format(node));
                    }
                    break;
                case HOURS:
                    long hours = TimeUnit.MILLISECONDS.toHours(duration);
                    for (long i = 0; i <= hours; i++) {
                        Date node = new Date(start.getTime() + TimeUnit.HOURS.toMillis(i));
                        timeNodes.add(dateFormat.format(node));
                    }
                    break;
                case MINUTES:
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
                    for (long i = 0; i <= minutes; i++) {
                        Date node = new Date(start.getTime() + TimeUnit.MINUTES.toMillis(i));
                        timeNodes.add(dateFormat.format(node));
                    }
                    break;
                case SECONDS:
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                    for (long i = 0; i <= seconds; i++) {
                        Date node = new Date(start.getTime() + TimeUnit.SECONDS.toMillis(i));
                        timeNodes.add(dateFormat.format(node));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported time unit: " + timeUnit);
            }
        } catch (Exception e) {
            log.info("时间处理失败！");
        }
        return timeNodes;
    }

    /**
     * 根据传入的字符串内容判断对应的时间格式化方式
     * @param inputDate
     * @return
     */
    private static SimpleDateFormat getDateFormat(String inputDate) {
        // 正则表达式匹配日期格式
        String regex = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(inputDate).matches()) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    }

    /**
     * 当前时间是否为早上00：00到06：00
     * @return
     */
    public static boolean isTimeInRange() {
        // 获取当前时间
        LocalTime currentTime = LocalTime.now();
        // 设置起始时间和结束时间
        LocalTime startTime = LocalTime.of(0, 0);  // 00:00
        LocalTime endTime = LocalTime.of(6, 0);    // 06:00
        return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime);
    }
}
