//package com.tianji.data.domain;
//
//import com.influxdb.annotations.Column;
//import com.influxdb.annotations.Measurement;
//import lombok.Data;
//import lombok.experimental.Accessors;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//
//@Data
//@Accessors(chain = true)
//@Measurement(name = "device_history_location")
//public class InfluxPosition {
//
//    /**
//     * 设备id
//     */
//    @Column(name = "device_id",tag = true)
//    private String deviceId;
//
//    /**
//     * 车辆id
//     */
//    @Column(name = "vehicle_id")
//    private String vehicleId;
//
//    /**
//     * 位置时间
//     */
//    @Column(timestamp = true)
//    private Instant locationTime;
//
//    /**
//     * 经度
//     */
//    @Column(name = "longitude")
//    private BigDecimal longitude;
//
//    /**
//     * 纬度
//     */
//    @Column(name = "latitude")
//    private BigDecimal latitude;
//
//    /**
//     * 速度(单位：km/h)
//     */
//    @Column(name = "speed")
//    private Integer speed;
//
//    /**
//     * 海拔(单位：米)
//     */
//    @Column(name = "altitude")
//    private Integer altitude;
//
//    /**
//     * 里程数(单位：米)
//     * m
//     */
//    @Column(name = "mileage")
//    private Integer mileage;
//}
