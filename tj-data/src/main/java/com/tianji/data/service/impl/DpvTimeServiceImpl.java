package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.data.mapper.DpvTimeMapper;
import com.tianji.data.model.po.DpvTime;
import com.tianji.data.service.IDpvTimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：用户活跃数时段服务接口实现类
 */
@Slf4j
@Service
public class DpvTimeServiceImpl extends ServiceImpl<DpvTimeMapper, DpvTime> implements IDpvTimeService {

}
