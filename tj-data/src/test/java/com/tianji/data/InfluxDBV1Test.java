//package com.tianji.data;/**
// * @author fsq
// * @date 2025/6/19 13:18
// */
//
//import com.influxdb.client.InfluxDBClient;
//import com.influxdb.client.QueryApi;
//import com.influxdb.client.WriteApi;
//import com.influxdb.client.WriteOptions;
//import com.influxdb.client.domain.WritePrecision;
//import com.influxdb.client.write.Point;
//import com.influxdb.query.FluxTable;
//import org.influxdb.InfluxDB;
//import org.influxdb.dto.Pong;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//
///**
// * @Author: fsq
// * @Date: 2025/6/19 13:18
// * @Version: 1.0
// */
//@SpringBootTest
//public class InfluxDBV1Test {
//
//    @Autowired
//    InfluxDBClient influxDBClient;
//    @Value("${spring.influx.org:''}")
//    private String org;
//
//    @Value("${spring.influx.bucket:''}")
//    private String bucket;
//
//    @Autowired
//    InfluxDB influxDB;
//    @Test
//    public void test() {
//        boolean ping = ping();
//        System.out.println(ping);
//    }
//
//    /**
//     * 测试连接是否正常
//     *
//     * @return true 正常
//     */
//    public boolean ping() {
//        boolean isConnected = false;
//        Pong pong;
//        try {
//            pong = influxDB.ping();
//            if (pong != null) {
//                isConnected = true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return isConnected;
//    }
//
//    @Test
//    public void save(String measurement, Map<String,Object> fields){
//        WriteOptions writeOptions = WriteOptions.builder()
//                .batchSize(5000)
//                .flushInterval(1000)
//                .bufferLimit(10000)
//                .jitterInterval(1000)
//                .retryInterval(5000)
//                .build();
//        try (WriteApi writeApi = influxDBClient.getWriteApi(writeOptions)) {
//            Point point = Point
//                    .measurement(measurement)
//                    .addFields(fields)
//                    .time(Instant.now(), WritePrecision.NS);
//            writeApi.writePoint(bucket, org, point);
//        }
//    }
//
//    @Test
//    public List<FluxTable> findAll() {
//        String flux = String.format("from(bucket: \"%s\") |> range(start: 0)", bucket);
//        QueryApi queryApi = influxDBClient.getQueryApi();
//        List<FluxTable> tables = queryApi.query(flux, org);
//        return tables;
//    }
//
//
//}
