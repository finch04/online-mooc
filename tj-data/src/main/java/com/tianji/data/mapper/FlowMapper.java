package com.tianji.data.mapper;

import com.tianji.data.influxdb.InfluxDBBaseMapper;
import com.tianji.data.influxdb.anno.Param;
import com.tianji.data.influxdb.anno.Select;
import com.tianji.data.influxdb.domain.BusinessLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FlowMapper extends InfluxDBBaseMapper {

    /**
     * 日报错次数（按日分组）
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日报错次数列表（按日期升序）
     */
    @Select(value = "SELECT COUNT(request_id) AS error_count " +
            "FROM log " +
            "WHERE time > #{begin} AND time <=  #{end} AND response_code != 200 " +
            "GROUP BY time(1d) " +
            "ORDER BY time ASC " ,
            resultType = Long.class,
            bucket = "point_data")
    List<Long> dpvForIndexByDay(@Param("begin") String begin, @Param("end") String end);

    /**
     * 日用户访问量（按日分组）
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日去重用户数列表（按日期升序）
     */
    @Select(value = "SELECT COUNT(DISTINCT(user_id)) AS unique_users " +
            "FROM log " +
            "WHERE time > #{begin} AND time <=  #{end} " +
            "GROUP BY time(1d) " +
            "ORDER BY time ASC " ,
            resultType = Long.class,
            bucket = "point_data")
    List<Long> duvByDay(@Param("begin") String begin, @Param("end") String end);


    /**
     * 每日新注册用户数（按日分组）
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日新注册用户数列表（按日期升序）
     */
    @Select(value = "SELECT COUNT(request_id) AS new_users " +
            "FROM log " +
            "WHERE response_code = '200' AND time > #{begin} AND time <=  #{end} " +
            "AND request_uri =~/\\\\/students\\\\/register\\\\//" +
            "GROUP BY time(1d) " +
            "FILL(0) "+
            "ORDER BY time ASC ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> dnuByDay(@Param("begin") String begin, @Param("end") String end);

    /**
     * 日访问量（按日分组）
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日访问量列表（按日期升序）
     */
    @Select(value = "SELECT COUNT(request_id) AS visits " +
            "FROM log " +
            "WHERE time > #{begin} AND time <=  #{end} " +
            "GROUP BY time(1d) " +
            "ORDER BY time ASC ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> dpvByDay(@Param("begin") String begin, @Param("end") String end);

    /**
     * 每日活跃用户数（按日分组）
     * @param begin 开始时间
     * @param end 结束时间
     * @return 每日活跃用户数列表（按日期升序）
     */
    @Select(value = "SELECT COUNT(DISTINCT(user_id)) AS active_users " +
            "FROM log " +
            "WHERE time > #{begin} AND time <=  #{end} " +
            "GROUP BY time(1d) " +
            "ORDER BY time ASC ",
            resultType = Long.class,
            bucket = "point_data")
    List<Long> allDauForUserIdByDay(@Param("begin") String begin, @Param("end") String end);


    /**
     * 每日所有活跃用户userIds
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT DISTINCT(user_id)  FROM  log  WHERE  time > #{begin} and time <=  #{end} ",resultType = String.class,bucket = "point_data")
    List<String> allDauForUserId(@Param("begin")String begin, @Param("end")String end);



    /**
     * 每日新注册用户request_body
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT response_body FROM log WHERE response_code = '200' and  time > #{begin} and time <=  #{end} and response_code = '200' and  request_uri =~/\\\\/students\\\\/register\\\\//",resultType = String.class,bucket = "point_data")
    List<String> newDnuForResponseBody(@Param("begin")String begin, @Param("end")String end);


    /**
     * 购买课程 访问日志列表
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT * FROM  log  WHERE  time > #{begin} and time < #{end} and response_code = '200' and request_uri =~/\\\\/orders\\\\/placeOrder/ ",resultType = BusinessLog.class,bucket = "point_data")
    List<BusinessLog> buyCourseList(@Param("begin")String begin, @Param("end")String end);



    /**
     * 课程介绍 访问日志列表
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT * FROM  log  WHERE  time > #{begin} and time < #{end} and response_code = '200' and request_uri =~/\\\\/courses\\\\/baseInfo/ ",resultType = BusinessLog.class,bucket = "point_data")
    List<BusinessLog> courseDetailList(@Param("begin")String begin, @Param("end")String end);
}