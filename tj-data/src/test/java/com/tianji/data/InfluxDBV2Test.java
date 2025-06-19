package com.tianji.data;/**
 * @author fsq
 * @date 2025/6/19 14:21
 */

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.tianji.common.utils.RandomUtils;
import com.tianji.data.domain.InfluxPosition;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/19 14:21
 * @Version: 1.0
 */
@SpringBootTest
public class InfluxDBV2Test {

    @Autowired
    private InfluxDBClient influxDBClient; // 自动注入客户端

    @Value("${spring.influx.org}")
    private String org;

    @Value("${spring.influx.bucket}")
    private String bucket;

    @Test
    public void testWriteAndQueryData() {
        // 写入数据
        writeData();

        // 查询数据
//        queryData();
    }

    @Test
    public void writeData() {
        WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();
        List<InfluxPosition> list = new ArrayList<>();
        // 创建基于东八区的当前时间
        Instant shanghaiTime = createShanghaiInstant();

        for (int i = 0; i < 100; i++) {
            InfluxPosition position = new InfluxPosition()
//                    .setDeviceId(RandomUtils.randomNumbers(4))
//                    .setVehicleId(RandomUtils.randomNumbers(4))
                    .setDeviceId("123")
                    .setVehicleId("222")
                    .setLocationTime(shanghaiTime)
                    .setLongitude(new BigDecimal("113.12313"))
                    .setLatitude(new BigDecimal("23.8524"))
                    .setSpeed(i * 10)
                    .setAltitude(i * 100)
                    .setMileage(i * 1000);
            list.add(position);
        }

        // 使用配置的 bucket 和 org
        writeApiBlocking.writeMeasurements(bucket, org, WritePrecision.MS, list);
        System.out.println("成功写入条数据"+ list.size());
    }

    @Test
    public void queryData() {
        // 使用配置的 bucket
        String flux = String.format("from(bucket: \"%s\")\n" +
                        "  |> range(start: -6h)\n" +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \"device_history_location\")\n" +
                        "  |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                bucket);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);
        List<InfluxPosition> positions = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                InfluxPosition position = new InfluxPosition();
                position.setDeviceId(record.getValueByKey("device_id").toString());
                position.setVehicleId(record.getValueByKey("vehicle_id").toString());
                position.setLocationTime(record.getTime());
                position.setLongitude(new BigDecimal(record.getValueByKey("longitude").toString()));
                position.setLatitude(new BigDecimal(record.getValueByKey("latitude").toString()));
                position.setSpeed(Integer.parseInt(record.getValueByKey("speed").toString()));
                position.setAltitude(Integer.parseInt(record.getValueByKey("altitude").toString()));
                position.setMileage(Integer.parseInt(record.getValueByKey("mileage").toString()));

                positions.add(position);
            }
        }

        // 打印对象的各个字段
        for (InfluxPosition position : positions) {
            System.out.println("Device ID: " + position.getDeviceId());
            System.out.println("Vehicle ID: " + position.getVehicleId());
            System.out.println("Location Time: " + convertToShanghaiTime(position.getLocationTime()));
            System.out.println("Longitude: " + position.getLongitude());
            System.out.println("Latitude: " + position.getLatitude());
            System.out.println("Speed: " + position.getSpeed());
            System.out.println("Altitude: " + position.getAltitude());
            System.out.println("Mileage: " + position.getMileage());
            System.out.println("------------------------");
        }

        System.out.println("查询完成，共返回对象 " + positions.size() + " 个");
    }

    // 辅助方法：将 UTC 时间转换为东八区字符串
    private String convertToShanghaiTime(Instant instant) {
        if (instant == null) {
            return "";
        }
        return instant.atZone(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    // 创建基于东八区的 Instant
    private Instant createShanghaiInstant() {
        // 获取东八区的当前时间
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        // 转换为 Instant（内部仍为 UTC，但表示的是东八区的时间点）
        return localDateTime.toInstant(ZoneOffset.ofHours(0));
    }

}
