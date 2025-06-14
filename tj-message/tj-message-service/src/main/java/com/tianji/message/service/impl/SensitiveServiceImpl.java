package com.tianji.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.message.domain.po.Sensitive;
import com.tianji.message.mapper.SensitiveMapper;
import com.tianji.message.service.ISensitiveService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensitiveServiceImpl extends ServiceImpl<SensitiveMapper, Sensitive> implements ISensitiveService {
    @Override
    public List<Sensitive> getAllSensitiveWords() {
        return this.list();
    }

    @Override
    public boolean saveSensitive(Sensitive sensitive) {
        //判断是否有重复的违禁词
        Integer count = this.lambdaQuery().eq(Sensitive::getSensitives, sensitive.getSensitives()).count();
        if(count>0){
            throw new BadRequestException("存在同名违禁词");
        }
        return this.save(sensitive);
    }

    @Override
    public boolean updateSensitive(Sensitive sensitive) {
        //判断是否有重复的违禁词
        Sensitive one = this.lambdaQuery().eq(Sensitive::getSensitives, sensitive.getSensitives()).one();
        if(!one.getId().equals(sensitive.getId())){
            throw new BadRequestException("存在同名违禁词");
        }
        return this.updateById(sensitive);
    }

    @Override
    public boolean deleteSensitive(Integer id) {
        Sensitive sensitive = this.getById(id);
        if(sensitive==null){
            throw new BadRequestException("不存在该条数据");
        }
        return this.removeById(id);
    }
}