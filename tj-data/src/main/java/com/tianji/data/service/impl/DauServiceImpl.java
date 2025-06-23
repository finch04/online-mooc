package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.data.mapper.DauMapper;
import com.tianji.data.model.po.Dau;
import com.tianji.data.service.IDauService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：用户日活跃数服务实现类
 */
@Slf4j
@Service
public class DauServiceImpl extends ServiceImpl<DauMapper, Dau> implements IDauService {

}
