package com.tianji.data.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * DateAvgUtil
 *
 * @author: fsq
 * @describe: 日期工具类
 * @date: 2025年6月23日12:06:25
 */
public class DateAvgUtil {


    /**
     * 获取本月的第一天的00:00:00
     * 和本月最后一天的 23:59:59
     * @return List<String> 包含时间节点的列表
     */
    public static List<String> getThisMonthBetweenTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> res = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();

        // 获取本月第一天的日期和时间
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date firstDayOfMonth = calendar.getTime();

        // 获取本月最后一天的日期和时间
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date lastDayOfMonth = calendar.getTime();

        // 将时间节点放入 MonthDTO 对象并添加到结果列表
        res.add(sdf.format(firstDayOfMonth));
        res.add(sdf.format(lastDayOfMonth));
        return res;

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
            e.printStackTrace();
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

//    public static void main(String[] args) {
//        System.out.println(getGraduallys("2015-05-05 00:00:01","2016-05-05 00:05:02",TimeUnit.DAYS));
//        System.out.println(getThisMonthBetweenTime());
//    }

}
