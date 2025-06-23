package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.data.mapper.DauTimeMapper;
import com.tianji.data.model.po.DauTime;
import com.tianji.data.service.IDauTimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：用户活跃数时段服务接口实现类
 */
@Slf4j
@Service
public class DauTimeServiceImpl extends ServiceImpl<DauTimeMapper, DauTime> implements IDauTimeService {

}
