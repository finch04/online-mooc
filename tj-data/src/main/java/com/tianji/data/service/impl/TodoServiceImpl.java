package com.tianji.data.service.impl;

import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.dto.TodayDataDTO;
import com.tianji.data.model.po.TodayDataInfo;
import com.tianji.data.model.vo.TodayDataVO;
import com.tianji.data.model.vo.TodoDataVO;
import com.tianji.data.service.TodoService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @ClassName TodoListServiceImpl
 * @author fsq
 * @since 2025-5-18 19:06:26
 **/
@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public TodoDataVO get() {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_TODO + DataUtils.getVersion(1);
        // 2.获取数据
        Object originData = redisTemplate.opsForValue().get(key);
        // 2.1.数据判空
        if (originData == null) {
            return new TodoDataVO();
        }
        return JsonUtils.toBean(originData.toString(), TodoDataVO.class);
    }

}
