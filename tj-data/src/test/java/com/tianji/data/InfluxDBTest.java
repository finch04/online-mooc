//package com.tianji.data;
//
//import com.influxdb.client.*;
//import com.influxdb.client.domain.Bucket;
//import com.influxdb.client.domain.BucketRetentionRules;
//import com.influxdb.client.domain.DeletePredicateRequest;
//import com.influxdb.client.domain.WritePrecision;
//import com.influxdb.client.write.Point;
//import com.influxdb.query.FluxRecord;
//import com.influxdb.query.FluxTable;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class InfluxDBTest {
//
//    @Autowired
//    private InfluxDBClient influxDBClient;
//
//    @Value("${spring.influx.org:my-org}")
//    private String org;
//
//    private String testBucket;
//
//    @BeforeEach
//    public void setUp() {
//        // 每次测试前创建唯一的测试 Bucket
//        testBucket = "test_bucket";
//        createBucket(testBucket);
//    }
//
//    // 1. 创建数据库（Bucket）
//    @Test
//    public void testCreateBucket() {
//        String bucketName = "new_test_bucket";
//        Bucket bucket = createBucket(bucketName);
//        assertNotNull(bucket);
//        assertEquals(bucketName, bucket.getName());
//        System.out.println("创建 Bucket 成功: " + bucketName);
//
//        // 清理测试数据
//        deleteBucket(bucketName);
//    }
//
//    // 2. 写入数据
//    @Test
//    public void testWriteData() {
//        // 准备测试数据
//        Map<String, Object> fields = new HashMap<>();
//        fields.put("temperature", 25.5);
//        fields.put("humidity", 60.2);
//        fields.put("pressure", 1013.25);
//
//        String measurement = "sensor_data";
//        save(measurement, fields);
//
//        // 验证数据是否写入
//        List<FluxTable> tables = findAll();
//        assertFalse(tables.isEmpty());
//        System.out.println("写入数据成功，共查询到 " + tables.size() + " 条记录");
//    }
//
//    // 3. 查询数据 - 按时间范围
//    @Test
//    public void testQueryByTimeRange() {
//        // 写入测试数据
//        writeTestSensorData();
//
//        // 查询最近1小时的数据
//        LocalDateTime end = LocalDateTime.now();
//        LocalDateTime start = end.minusHours(1);
//
//        List<FluxTable> tables = queryByTimeRange("sensor_data", start, end);
//        assertFalse(tables.isEmpty());
//
//        // 打印查询结果
//        for (FluxTable table : tables) {
//            for (FluxRecord record : table.getRecords()) {
//                System.out.printf(
//                    "时间: %s, 温度: %s, 湿度: %s, 压力: %s%n",
//                    record.getTime(),
//                    record.getValueByKey("temperature"),
//                    record.getValueByKey("humidity"),
//                    record.getValueByKey("pressure")
//                );
//            }
//        }
//    }
//
//    // 4. 更新数据（InfluxDB 中通过覆盖写入实现）
//    @Test
//    public void testUpdateData() {
//        // 写入原始数据
//        Map<String, Object> fields = new HashMap<>();
//        fields.put("temperature", 25.0);
//        fields.put("humidity", 50.0);
//        save("sensor_data", fields);
//
//        // 查询原始数据
//        List<FluxTable> tables = findAll();
//        FluxRecord record = tables.get(0).getRecords().get(0);
//        Instant timestamp = record.getTime();
//        assertEquals(25.0, record.getValueByKey("temperature"));
//
//        // 更新数据（使用相同时间戳写入新值）
//        Map<String, Object> updatedFields = new HashMap<>();
//        updatedFields.put("temperature", 26.5); // 更新温度值
//        updatedFields.put("humidity", 55.0);    // 更新湿度值
//
//        Point point = Point
//            .measurement("sensor_data")
//            .addFields(updatedFields)
//            .time(timestamp, WritePrecision.NS);
//
//        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
//            writeApi.writePoint(testBucket, org, point);
//        }
//
//        // 验证更新结果
//        tables = findAll();
//        record = tables.get(0).getRecords().get(0);
//        assertEquals(26.5, record.getValueByKey("temperature"));
//        assertEquals(55.0, record.getValueByKey("humidity"));
//        System.out.println("数据更新成功");
//    }
//
//    // 5. 删除数据
//    @Test
//    public void testDeleteData() {
//        // 写入测试数据
//        writeTestSensorData();
//
//        // 验证数据存在
//        List<FluxTable> tablesBeforeDelete = findAll();
//        assertFalse(tablesBeforeDelete.isEmpty());
//
//        // 删除最近1小时的数据
//        LocalDateTime start = LocalDateTime.now().minusHours(1);
//        LocalDateTime end = LocalDateTime.now();
//        deleteByTimeRange("sensor_data", start, end);
//
//        // 验证数据已删除
//        List<FluxTable> tablesAfterDelete = findAll();
//        assertTrue(tablesAfterDelete.isEmpty());
//        System.out.println("数据删除成功");
//    }
//
//    // ============== 辅助方法 ==============
//
//    private Bucket createBucket(String bucketName) {
//        BucketsApi bucketsApi = influxDBClient.getBucketsApi();
//
//        // 检查 Bucket 是否已存在
//        Bucket bucket = bucketsApi.findBucketByName(bucketName);
//        if (bucket != null) {
//            return bucket;
//        }
//
//        // 创建新 Bucket
//        BucketRetentionRules retentionRules = new BucketRetentionRules();
//        retentionRules.setEverySeconds(3600 * 24 * 7); // 7天保留期
//        return bucketsApi.createBucket(bucketName, retentionRules, org);
//    }
//
//    private void deleteBucket(String bucketName) {
//        BucketsApi bucketsApi = influxDBClient.getBucketsApi();
//        Bucket bucket = bucketsApi.findBucketByName(bucketName);
//        if (bucket != null) {
//            influxDBClient.getBucketsApi().deleteBucket(bucket);
//        }
//    }
//
//    private void save(String measurement, Map<String, Object> fields) {
//        WriteOptions writeOptions = WriteOptions.builder()
//                .batchSize(5000)
//                .flushInterval(1000)
//                .bufferLimit(10000)
//                .jitterInterval(1000)
//                .retryInterval(5000)
//                .build();
//
//        try (WriteApi writeApi = influxDBClient.getWriteApi(writeOptions)) {
//            Point point = Point
//                    .measurement(measurement)
//                    .addFields(fields)
//                    .time(Instant.now(), WritePrecision.NS);
//            writeApi.writePoint(testBucket, org, point);
//        }
//    }
//
//    private List<FluxTable> findAll() {
//        String flux = String.format("from(bucket: \"%s\") |> range(start: 0)", testBucket);
//        QueryApi queryApi = influxDBClient.getQueryApi();
//        return queryApi.query(flux, org);
//    }
//
//    private List<FluxTable> queryByTimeRange(String measurement, LocalDateTime start, LocalDateTime end) {
//        String flux = String.format(
//            "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r._measurement == \"%s\")",
//            testBucket,
//            formatTime(start),
//            formatTime(end),
//            measurement
//        );
//
//        QueryApi queryApi = influxDBClient.getQueryApi();
//        return queryApi.query(flux, org);
//    }
//
//    private void deleteByTimeRange(String measurement, LocalDateTime start, LocalDateTime end) {
//        DeleteApi deleteApi = influxDBClient.getDeleteApi();
//
//        DeletePredicateRequest predicate = new DeletePredicateRequest();
//        predicate.setStart(formatTime(start));
//        predicate.setStop(formatTime(end));
//        predicate.setPredicate(String.format("_measurement=\"%s\"", measurement));
//
//        deleteApi.delete(predicate, org, testBucket);
//    }
//
//    private OffsetDateTime formatTime(LocalDateTime time) {
//        // 将 LocalDateTime 转换为 UTC 时区的 OffsetDateTime
//        return time.atOffset(ZoneOffset.UTC);
//    }
//
//    private void writeTestSensorData() {
//        // 写入5条测试数据
//        WriteApi writeApi = influxDBClient.getWriteApi();
//
//        for (int i = 0; i < 5; i++) {
//            LocalDateTime time = LocalDateTime.now().minusMinutes(i * 10);
//            Instant instant = time.toInstant(ZoneOffset.UTC);
//
//            Map<String, Object> fields = new HashMap<>();
//            fields.put("temperature", 22.0 + i * 0.5);
//            fields.put("humidity", 50.0 + i * 2.0);
//            fields.put("pressure", 1013.25 + i * 0.1);
//
//            Point point = Point
//                .measurement("sensor_data")
//                .addFields(fields)
//                .time(instant, WritePrecision.NS);
//
//            writeApi.writePoint(testBucket, org, point);
//        }
//
//        writeApi.close();
//    }
//}