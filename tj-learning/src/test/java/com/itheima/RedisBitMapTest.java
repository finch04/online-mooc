package com.itheima;


import com.tianji.learning.LearningApplication;
import io.swagger.annotations.ApiModelProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@SpringBootTest(classes = LearningApplication.class)
public class RedisBitMapTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void test(){
        //对test116 第五天 做签到
        //返回结果代表offset为4 原来的值

        Boolean setBit = redisTemplate.opsForValue().setBit("test116", 4, true);
        System.out.println(setBit);
        if(setBit){
            //说明当天已经签过到了
        }
    }

    @Test
    public void test1(){
        //去第一天到第三天的签到记录 redis的bitMap存的是二进制，取出来的是十进制
        //bitfield test116 get u3 0 取test116 从第一位开始取，取3位转换为无符号十进制
        List<Long> list = redisTemplate.opsForValue()
                .bitField("test116", BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(3)).valueAt(0));
        Long aLong = list.get(0);
        System.out.println(aLong);
    }

}
