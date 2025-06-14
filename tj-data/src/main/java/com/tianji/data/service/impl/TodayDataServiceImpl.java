package com.tianji.data.service.impl;

import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.dto.TodayDataDTO;
import com.tianji.data.model.po.TodayDataInfo;
import com.tianji.data.model.vo.TodayDataVO;
import com.tianji.data.service.TodayDataService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    @Override
    public void set(TodayDataDTO todayDataDTO) {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_TODAY + todayDataDTO.getVersion();
        // 2.数据转化
        TodayDataInfo todayDataInfo = BeanUtils.toBean(todayDataDTO, TodayDataInfo.class);
        // 3.数据存储
        redisTemplate.opsForValue().set(key, JsonUtils.toJsonStr(todayDataInfo));
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
