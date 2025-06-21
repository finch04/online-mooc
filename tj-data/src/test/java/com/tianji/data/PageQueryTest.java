//package com.tianji.data;/**
// * @author fsq
// * @date 2025/6/21 12:44
// */
//
//import com.tianji.common.domain.dto.PageDTO;
//import com.tianji.data.influxdb.domain.BusinessLog;
//import com.tianji.data.influxdb.service.IUrlAnalysisService;
//import com.tianji.data.influxdb.service.impl.UrlAnalysisServiceImpl;
//import com.tianji.data.model.query.UrlPageQuery;
//import com.tianji.data.model.query.UrlQuery;
//import com.tianji.data.model.vo.EchartsVO;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//
///**
// * @Author: fsq
// * @Date: 2025/6/21 12:44
// * @Version: 1.0
// */
//@SpringBootTest
//public class PageQueryTest {
//
//    @Autowired
//    private IUrlAnalysisService urlAnalysisService;
//
//
//    @Test
//    public void testAnalysis() {
//        UrlPageQuery query = new UrlPageQuery();
//        query.setPageNo(1);
//        query.setPageSize(10);
//        query.setSortBy("id");
//        query.setIsAsc(true);
//        query.setUrl("/data/board");
//        query.setBeginTime(LocalDateTime.now().minusDays(7));
//        query.setEndTime(LocalDateTime.now());
//        PageDTO<BusinessLog> businessLogPageDTO = urlAnalysisService.analyzeUrl(query);
//        for (BusinessLog businessLog : businessLogPageDTO.getList()) {
//            System.out.println(businessLog);
//        }
//    }
//
//    @Test
//    public void testAnalysisByLike() {
//        UrlPageQuery query = new UrlPageQuery();
//        query.setPageNo(1);
//        query.setPageSize(10);
//        query.setSortBy("id");
//        query.setIsAsc(true);
//        query.setUrl("/data");
//        query.setBeginTime(LocalDateTime.now().minusDays(7));
//        query.setEndTime(LocalDateTime.now());
//        PageDTO<BusinessLog> businessLogPageDTO = urlAnalysisService.analyzeUrlByLike(query);
//        for (BusinessLog businessLog : businessLogPageDTO.getList()) {
//            System.out.println(businessLog);
//        }
//    }
//
//    @Test
//    public void testUrlAnalysis() {
//        UrlQuery query = new UrlQuery();
//        query.setUrl("/data/board");
//        query.setBeginTime(LocalDateTime.now().minusDays(7));
//        query.setEndTime(LocalDateTime.now());
//        EchartsVO metricByUrl = urlAnalysisService.getMetricByUrl(query);
//        System.out.println(metricByUrl);
//    }
//}
