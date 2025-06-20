//package com.tianji.data;/**
// * @author fsq
// * @date 2025/6/19 17:22
// */
//
//import com.tianji.common.domain.vo.LogBusinessVO;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//
///**
// * @Author: fsq
// * @Date: 2025/6/19 17:22
// * @Version: 1.0
// */
//@SpringBootTest
//public class ServiceTest {
//
//    @Autowired
//    private LogBusinessService logBusinessService;
//
//    @Test
//    public void test() {
//        List<LogBusinessVO> logBusinessVOS =
//                logBusinessService.queryLogsByTimeRange(
//                        Instant.now().minus(24, ChronoUnit.HOURS),
//                        Instant.now());
//        System.out.println("查询完毕"+logBusinessVOS.size());
//        logBusinessVOS.forEach(System.out::println);
//    }
//}
