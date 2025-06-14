package com.tianji.message.thirdparty;/**
 * @author fsq
 * @date 2025/6/14 18:55
 */

import com.tianji.message.utils.SensitiveWordDetector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * @Author: fsq
 * @Date: 2025/6/14 18:55
 * @Version: 1.0
 */
@SpringBootTest
public class SensitiveTest {

    @Test
    public void test() throws Exception {
//        List<String> list = new ArrayList<>();
//        list.add("法轮");
//        list.add("法轮功");
//        list.add("冰毒");
        String content="我是一个好人，并不会卖冰毒，也不操练法轮功,我真的不卖冰毒";
        boolean b = SensitiveWordDetector.containsSensitiveWord(content);
        System.out.println(b);
    }
}
