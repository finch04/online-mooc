package com.tianji.data.model.vo;

import com.tianji.common.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AxisVO
 * @Author wusongsong
 * @Date 2022/10/10 10:55
 * @Version
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class AxisVO {

    //数值轴
    public static final String TYPE_VALUE = "value";
    //类目轴
    public static final String TYPE_CATEGORY = "category";
    //时间轴
    public static final String TYPE_TIME = "time";
    //对数轴
    public static final String TYPE_LOG = "log";

    private String type;
    //最大值 带单位
    private Double max;
    //最小值 带单位
    private Double min;
    //平均值不带单位
    private Double average;
    // 数据
    private List<?> data;
    //interval
    private Double interval;


    /**
     * 根据开始时间和结束时间生成日期范围轴
     */
    public static AxisVO ofDateRange(String beginTime, String endTime) {
        List<String> dateList = generateDateList(beginTime, endTime);
        return AxisVO.builder()
                .type(TYPE_CATEGORY)
                .data(dateList)
                .build();
    }
    /**
     * 生成指定日期范围内的所有日期（支持带时间的格式）
     */
    private static List<String> generateDateList(String beginTime, String endTime) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter;

        // 检测输入格式是否包含时间部分
        if (beginTime.contains(" ") && beginTime.length() > 10) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        try {
            LocalDateTime start = LocalDateTime.parse(beginTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);

            // 转换为LocalDate进行日期计算
            LocalDate startDate = start.toLocalDate();
            LocalDate endDate = end.toLocalDate();

            while (!startDate.isAfter(endDate)) {
                dates.add(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                startDate = startDate.plusDays(1);
            }
        } catch (Exception e) {
            log.error("日期解析失败，输入格式: beginTime={}, endTime={}", beginTime, endTime, e);
        }
        return dates;
    }

    public static AxisVO last15Day() {

        return new AxisVO(
                TYPE_CATEGORY,
                null,
                null,
                null,
                DateUtils.last15Day(),
                null
        );
    }

}
