package com.tianji.data.influxdb.mapper;

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
     * 统计URL在指定时间范围内的总访问量
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 开始时间
     * @param end 结束时间
     * @return 总访问量
     */
    @Select(value = "SELECT COUNT(request_id) AS total_visits " +
            "FROM log " +
            "WHERE time > #{begin} AND time < #{end} AND request_uri =#{urlRegex}",
            resultType = Long.class,
            bucket = "point_data")
    Long countTotalVisits(
            @Param("urlRegex") String urlRegex,
            @Param("begin") String begin,
            @Param("end") String end);

    /**
     * 统计URL在指定时间范围内的每日访问量
     * @param urlRegex 要匹配的URL
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日访问量列表
     */
    @Select(value = "SELECT COUNT(request_id) FROM log " +
            "WHERE time > #{begin} AND time < #{end} AND request_uri = #{urlRegex} " +
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
            "WHERE time > #{begin} AND time < #{end} " +
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
            "WHERE time > #{begin} AND time < #{end} " +
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
            "WHERE time > #{begin} AND time < #{end} " +
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
     * 统计模糊URL在今天的日志数量
     * @param urlRegex 要匹配的URL正则表达式
     * @param begin 今天的开始时间
     * @param end 今天的结束时间
     * @return 日志数量
     */
    @Select(value = "SELECT COUNT(*) " +
            "FROM log " +
            "WHERE time > #{begin} AND time < #{end} " +
            "AND request_uri =~${urlRegex}",
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
            "WHERE time > #{begin} AND time < #{end} " +
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


    /**
     * 每日登录用户
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT * FROM log WHERE response_code = '200' and  time > #{begin} and time < #{end} and request_uri =~/\\/login/", resultType = BusinessLog.class, bucket = "point_data")
    List<BusinessLog> login(@Param("begin") String begin, @Param("end")String end);

    /**
     * 每日新注册用户
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT * FROM log WHERE response_code = '200' and  time > #{begin} and time < #{end} and request_uri =~/\\\\/students\\\\/register\\\\//", resultType = BusinessLog.class, bucket = "point_data")
    List<BusinessLog> dnu(@Param("begin") String begin, @Param("end")String end);

    /**
     * 每日所有活跃用户userIds
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT DISTINCT(user_id)  FROM  log  WHERE  time > #{begin} and time < #{end} ",resultType = String.class,bucket = "point_data")
    List<String> allDauForUserId(@Param("begin")String begin, @Param("end")String end);

    /**
     * 每日新注册用户request_body
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT response_body FROM log WHERE response_code = '200' and  time > #{begin} and time < #{end} and request_uri =~/\\\\/students\\\\/register\\\\//",resultType = String.class,bucket = "point_data")
    List<String> newDnuForResponseBody(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日访问量
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(request_id) FROM  log  WHERE  time > #{begin} and time < #{end} ",resultType = Integer.class,bucket = "point_data")
    Integer dpv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日用户访问量
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(DISTINCT(user_id)) FROM  log  WHERE  time > #{begin} and time < #{end} ",resultType = Integer.class,bucket = "point_data")
    Integer duv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日报错次数
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(request_id) FROM  log  WHERE  time > #{begin} and time < #{end} and response_code != 200",resultType = BusinessLog.class,bucket = "point_data")
    Integer dpvForIndex(@Param("begin")String begin, @Param("end")String end);
}
