package com.tianji.data.influxdb.mapper;

import com.tianji.data.influxdb.InfluxDBBaseMapper;
import com.tianji.data.influxdb.anno.Param;
import com.tianji.data.influxdb.anno.Select;
import com.tianji.data.influxdb.domain.BusinessLog;
import org.apache.ibatis.annotations.Mapper;

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
    @Select(value = "SELECT response_body FROM log WHERE response_code = '200' and  time > #{begin} and time < #{end} and request_uri =~/register-user/",resultType = String.class,bucket = "point_data")
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
     * 每日点击首页业务记录
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT * FROM log WHERE response_code = '200' and  time > #{begin} and time < #{end} and request_uri =~/find-index-category-product/",resultType = BusinessLog.class,bucket = "point_data")
    List<BusinessLog> dpvForIndex(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日投保访问失败页面量
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(request_id) FROM  log  WHERE  time > #{begin} and time < #{end} and request_uri =~/do-insure/ and response_code != 200 ",resultType = Integer.class,bucket = "point_data")
    Integer doInsureFailDpv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日投保访问页面量
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(request_id) FROM  log  WHERE  time > #{begin} and time < #{end} and request_uri =~/do-insure/",resultType = Integer.class,bucket = "point_data")
    Integer doInsureDpv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日保险浏览页面量
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(request_id) FROM log WHERE  time > #{begin} and time < #{end} and request_uri =~/.*find-insurance\\/.*/",resultType = Integer.class,bucket = "point_data")
    Integer doBrowseDpv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日投保试算次数
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(request_id) FROM log WHERE  time > #{begin} and time < #{end} and (request_uri =~/do-premium/ or request_uri =~/do-earnings/ )",resultType = Integer.class,bucket = "point_data")
    Integer doTrialDpv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日投保用户访问数
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(DISTINCT(user_id)) FROM  log  WHERE  time > #{begin} and time < #{end} and request_uri =~/do-insure/",resultType = Integer.class,bucket = "point_data")
    Integer doInsureDuv(@Param("begin")String begin, @Param("end")String end);

    /**
     * 日保险分类访问记录
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT response_body FROM log WHERE  time > #{begin} and time < #{end} and request_uri =~/.*find-insurance\\/.*/",resultType = String.class,bucket = "point_data")
    List<String> categoryDpvForResponseBody(@Param("begin")String begin, @Param("end")String end);

    /**
     * 性别日投保访问页面量
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT COUNT(DISTINCT(user_id)) FROM  log  WHERE  time > #{begin} and time < #{end} and request_uri =~/do-insure/ and sex =#{sex}",resultType = Integer.class,bucket = "point_data")
    Integer doInsureGenderDuv(@Param("begin")String begin, @Param("end")String end, @Param("sex")String sex);

    /**
     * 计划任务：城市日投保用户访问数
     * @param begin
     * @param end
     * @return
     */
    @Select(value = "SELECT * FROM  log  WHERE  time > #{begin} and time < #{end} and request_uri =~/do-insure/ ",resultType = BusinessLog.class,bucket = "point_data")
    List<BusinessLog> doInsureCityDuv(@Param("begin")String begin, @Param("end")String end);
}
