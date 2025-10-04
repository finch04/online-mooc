package com.tianji.unqid.service.impl;

import com.tianji.unqid.constants.IDConstants;
import com.tianji.unqid.service.IGenerateIDService;
import jakarta.annotation.Resource;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Service;



@Service
public class GenerateIDServiceImpl implements IGenerateIDService {

    @Resource
    private IdGeneratorProvider idGeneratorProvider;

    @Override
    public Long getSeqId() {
        return idGeneratorProvider.get(IDConstants.ID_SEQUENCE).get().generate();
    }

    @Override
    public Long getUnSeqId() {
        return idGeneratorProvider.get(IDConstants.ID_SNOWFLAKE).get().generate();
    }
}
