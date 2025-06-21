package com.tianji.data; /**
 * @author fsq
 * @date 2025/6/21 17:17
 */

import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/21 17:17
 * @Version: 1.0
 */
@SpringBootTest
public class MapperTest {
    @Autowired
    private BusinessLogMapper businessLogMapper;

    @Test
    public void test() throws Exception {
        String url = "/data/board";
        String beginTime = LocalDate.now().minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00:00";
        String endTime = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + " 23:59:59";

        List<Long> longs = businessLogMapper.countDailyVisits(url, beginTime, endTime);
        for (Long aLong : longs) {
            System.out.println(aLong);
        }
    }
}
