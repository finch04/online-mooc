package com.tianji.data.mapper;

import com.tianji.data.influxdb.InfluxDBBaseMapper;
import com.tianji.data.influxdb.anno.Param;
import com.tianji.data.influxdb.anno.Select;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.domain.UrlMetrics;
import org.apache.ibatis.annotations.Mapper;
import retrofit2.http.Query;

import java.util.List;

/**
 * BusinessLogMapper
 *
 * @describe: 数据埋点日志持久层（influxDB）
 * @date: 2022/12/28 10:10
 */
@Mapper
public interface BusinessLogMapper extends InfluxDBBaseMapper {

    /**
     * 统计URL在指定时间范围内的每日访问量
     * @param urlRegex 要匹配的URL
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日访问量列表
     */
    @Select(value = "SELECT COUNT(request_id) FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} AND request_uri = #{urlRegex} " +
            "GROUP BY time(1d) ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> countDailyVisits(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end
    );

    /**
     * 统计URL在指定时间范围内的每日访问失败数
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 开始时间
     * @param end 结束时间
     * @return 失败访问量
     */
    @Select(value = "SELECT COUNT(request_id) AS failed_visits " +
            "FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} " +
            "AND request_uri =#{urlRegex}" +
            "AND response_code != '200'"+
            "GROUP BY time(1d) ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> countFailedVisits(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end);


    /**
     * 统计指定URL在今天的日志数量
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 今天的开始时间
     * @param end 今天的结束时间
     * @return 日志数量
     */
    @Select(value = "SELECT COUNT(*) " +
            "FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} " +
            "AND request_uri = #{urlRegex}",
            resultType = Long.class,
            bucket = "point_data")
    Long countLogsByUrlToday(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end
    );

    /**
     * 查询URL在指定时间范围内的详细日志记录
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 开始时间
     * @param end 结束时间
     * @return 详细日志列表
     */
    @Select(value = "SELECT * " +
            "FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} " +
            "AND request_uri = #{urlRegex} " +
            "LIMIT #{pageSize} OFFSET #{offset}",
            resultType = BusinessLog.class,
            bucket = "point_data")
    List<BusinessLog> findLogsByUrl(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset
    );

    /**
     * 导出全部日志
     * @return 详细日志列表
     */
    @Select(value = "SELECT * FROM log WHERE time > #{begin} AND time  <= #{end} ",
            resultType = BusinessLog.class,
            bucket = "point_data")
    List<BusinessLog> getAllLogsByTime(
            @Param("begin") String begin,
            @Param("end") String end
    );

    /**
     * 统计URL在指定时间范围内的每日访问量
     * @param urlRegex 要匹配的URL
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日访问量列表
     */
    @Select(value = "SELECT COUNT(request_id) FROM log " +
            "WHERE request_uri =~${urlRegex} " +
            "AND time > #{begin} AND time <= #{end} " +
            "GROUP BY time(1d) ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> countDailyVisitsByLike(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end
    );

    /**
     * 统计URL在指定时间范围内的每日访问失败数
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 开始时间
     * @param end 结束时间
     * @return 失败访问量
     */
    @Select(value = "SELECT COUNT(request_id) AS failed_visits " +
            "FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} " +
            "AND request_uri =~${urlRegex} " +
            "AND response_code != '200'"+
            "GROUP BY time(1d) ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> countFailedVisitsByLike(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end);

    /**
     * 统计模糊URL在今天的日志数量
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 今天的开始时间
     * @param end 今天的结束时间
     * @return 日志数量
     */
    @Select(value = "SELECT COUNT(*) " +
            "FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} " +
            "AND request_uri =~${urlRegex} ",
            resultType = Long.class,
            bucket = "point_data")
    Long countLogsByUrlTodayByLike(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end
    );

    /**
     * 模糊查询URL在指定时间范围内的详细日志记录
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 开始时间
     * @param end 结束时间
     * @return 详细日志列表
     */
    @Select(value = "SELECT * " +
            "FROM log " +
            "WHERE time > #{begin} AND time  <= #{end} " +
            "AND request_uri =~${urlRegex} " +
            "LIMIT #{pageSize} OFFSET #{offset}",
            resultType = BusinessLog.class,
            bucket = "point_data")
    List<BusinessLog> findLogsByUrlByLike(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset
    );

}
