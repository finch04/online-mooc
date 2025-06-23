package com.tianji.data.service.impl;

import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.dto.TodayDataDTO;
import com.tianji.data.model.vo.TodayDataVO;
import com.tianji.data.service.TodayDataService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName TodayDataServiceImpl
 * @Author wusongsong
 * @Date 2022/10/13 9:28
 * @Version
 **/
@Service
public class TodayDataServiceImpl implements TodayDataService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public TodayDataVO get() {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_TODAY + DataUtils.getVersion(1);
        // 2.获取数据
        Object originData = redisTemplate.opsForValue().get(key);
        // 2.1.数据判空
        if (originData == null) {
            return new TodayDataVO();
        }

        //这里获取一下今日访问量，因为也是从redis获取数据，性能很高
        //TODO 这里前端展示单位是万，但是除10000难看出效果，所以还是先不除了，先记着
        TodayDataVO vo = JsonUtils.toBean(originData.toString(), TodayDataVO.class);
        vo.setVisits(getTodayVisitCount().doubleValue());
        return vo;
    }

    //这里不需要将对象都传好，哪怕只传一个字段也能实现更新
    @Override
    public void set(TodayDataDTO todayDataDTO) {
        // 获取版本号
        Integer version = DataUtils.getVersion(1);
        String key = RedisConstants.KEY_TODAY + version;

        // 1. 从 Redis 获取现有数据
        String jsonData = (String) redisTemplate.opsForValue().get(key);

        // 2. 反序列化为对象（如果不存在则创建新对象）
        TodayDataVO existingData = (jsonData != null)
                ? JsonUtils.toBean(jsonData, TodayDataVO.class)
                : new TodayDataVO();

        // 3. 使用 DTO 中的非空字段更新现有数据
        if (todayDataDTO.getOrderAmount() != null) {
            // 处理金额字段，使用 BigDecimal 避免精度问题
            BigDecimal amount = BigDecimal.valueOf(todayDataDTO.getOrderAmount())
                    .setScale(2, RoundingMode.HALF_UP);
            existingData.setOrderAmount(amount.doubleValue());
        }

        if (todayDataDTO.getVisits() != null) {
            // 处理访问量字段，保留两位小数
            BigDecimal visits = BigDecimal.valueOf(todayDataDTO.getVisits())
                    .setScale(2, RoundingMode.HALF_UP);
            existingData.setVisits(visits.doubleValue());
        }

        if (todayDataDTO.getOrderNum() != null) {
            existingData.setOrderNum(todayDataDTO.getOrderNum());
        }

        if (todayDataDTO.getStuNewNum() != null) {
            existingData.setStuNewNum(todayDataDTO.getStuNewNum());
        }

        // 4. 将更新后的对象转回 JSON 存入 Redis
        redisTemplate.opsForValue().set(key, JsonUtils.toJsonStr(existingData));
    }
    /**
     * 获取今日访问量（去重后的独立用户数）
     * @return 今日访问量
     */
    public Long getTodayVisitCount() {
        String todayKey = RedisConstants.SYSTEM_VISIT_DAILY + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return redisTemplate.opsForHyperLogLog().size(todayKey);
    }

}
