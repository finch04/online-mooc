package com.tianji.learning.service.impl;

import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.ISignRecordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignRecordServiceImpl implements ISignRecordsService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;

    //签到
    @Override
    public SignResultVO addSignRecords() {
        Long userId = UserContext.getUser();
        //拼接key
        LocalDate now =LocalDate.now();//得到当前的年月
        String format = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));//得到冒号年月的字符串
        String key= RedisConstants.SIGN_RECORD_KEY_PREFIX+userId.toString()+format;

        //利用bitset命令，将签到记录保存到redis的bitmap结构中，需要校验是否已经签到
        int offset=now.getDayOfMonth()-1;//当前天数-1
        Boolean setBit = redisTemplate.opsForValue().setBit(key, offset, true);//最后一个参数是 是否签到
        if(setBit){
            throw new BizIllegalException("不能重复签到");
        }
        //计算连续签到的天数
        int days =countSignDays(key,now.getDayOfMonth());

        //计算连续签到的奖励积分
        int rewardPoints=0;//代表连续签到 奖励积分
        switch(days){
            case 7:
                rewardPoints=10;
                break;
            case 14:
                rewardPoints=20;
                break;
            case 28:
                rewardPoints=40;
                break;
        }
//        if(days==7){
//            rewardPoints=10;
//        }else if(days==14){
//            rewardPoints=20;
//        }else if(days==28){
//            rewardPoints=40;
//        }

        //保存积分
        mqHelper.send(MqConstants.Exchange.LEARNING_EXCHANGE,
                MqConstants.Key.SIGN_IN,
                SignInMessage.of(userId,rewardPoints+1)
        );

        //封装vo返回
        SignResultVO vo=new SignResultVO();
        vo.setSignDays(days);
        vo.setRewardPoints(rewardPoints);


        return vo;
    }

    /**
     * 计算签到连续多少天
     * @param key
     * @param dayOfMonth 本月到今天的天数
     * @return
     */
    private int countSignDays(String key, int dayOfMonth) {
        //求本月第一天到当前天所有的签到数据
        List<Long> bitField = redisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create().
                        get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if(CollUtils.isEmpty(bitField)){
            return 0;
        }
        Long num = bitField.get(0);//本月第一天到当前天的签到数据
        log.debug("num:{}",num);
        //num转二进制，从后向前推有几个1
        int counter = 0;//计数器
        while((num&1)==1){
            counter++;
            num=num>>>1;
        }
        return counter;
    }

    //查询签到记录
    @Override
    public Byte[] querySignRecords() {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.获取日期
        LocalDate now = LocalDate.now();
        int dayOfMonth = now.getDayOfMonth();
        // 3.拼接key
        String key = RedisConstants.SIGN_RECORD_KEY_PREFIX
                + userId
                + now.format(DateUtils.SIGN_DATE_SUFFIX_FORMATTER);
        // 4.读取
        List<Long> result = redisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands.create().get(
                        BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (CollUtils.isEmpty(result)) {
            return new Byte[0];
        }
        int num = result.get(0).intValue();

        Byte[] arr = new Byte[dayOfMonth];
        int pos = dayOfMonth - 1;
        while (pos >= 0){
            arr[pos--] = (byte)(num & 1);
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return arr;
    }
}
