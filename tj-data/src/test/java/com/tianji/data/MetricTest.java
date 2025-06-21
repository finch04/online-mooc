//package com.tianji.data;
//import com.tianji.data.influxdb.domain.BusinessLog;
//import com.tianji.data.influxdb.domain.UrlMetrics;
//import com.tianji.data.influxdb.service.IUrlAnalysisService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
///**
// * @Author: fsq
// * @Date: 2025/6/20 17:31
// * @Version: 1.0
// */
//@SpringBootTest
//public class MetricTest {
//
//    @Autowired
//    private IUrlAnalysisService urlAnalysisService;
//
//    @Test
//    public void testMetric(){
//            // 分析注册URL的访问情况
//            String url = "/accounts/login";
//            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//            String beginTime = today + " 00:00:00";
//            String endTime = today + " 23:59:59";
//
//            List<BusinessLog> metrics = urlAnalysisService.analyzeUrl(url, beginTime, endTime);
//
//            System.out.println("URL: " + url);
//
//            System.out.println("Total Visits: " + metrics.size());
//            System.out.println("Successful Visits: " + metrics.stream().filter(log -> log.getResponseCode().equals("200")).count());
//            for (BusinessLog metric : metrics) {
//                System.out.println(metric.toString());
//            }
//    }
//
//    @Test
//    public void testMetricLike(){
//        // 分析注册URL的访问情况
//        String url = "/login";
//        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//        String beginTime = today + " 00:00:00";
//        String endTime = today + " 23:59:59";
//
//        List<BusinessLog> metrics = urlAnalysisService.analyzeUrlByLike(url, beginTime, endTime);
//
//        System.out.println("URL: " + url);
//
//        System.out.println("Total Visits: " + metrics.size());
//        System.out.println("Successful Visits: " + metrics.stream().filter(log -> log.getResponseCode().equals("200")).count());
//        for (BusinessLog metric : metrics) {
//            System.out.println(metric.toString());
//        }
//    }
//}
