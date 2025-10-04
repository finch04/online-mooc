package com.tianji.live.service.impl;

import com.tianji.live.service.IGenerateIDRPCService;
import jakarta.annotation.Resource;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Service;

import static com.tianji.live.constants.IMConstants.ID_SEQUENCE;
import static com.tianji.live.constants.IMConstants.ID_SNOWFLAKE;

/**
 * Author： roy
 * Description：
 **/
@Service
public class GenerateIDRPCService implements IGenerateIDRPCService {

    @Resource
    private IdGeneratorProvider idGeneratorProvider;

    @Override
    public Long getSeqId() {
        return idGeneratorProvider.get(ID_SEQUENCE).get().generate();
    }

    @Override
    public Long getUnSeqId() {
        return idGeneratorProvider.get(ID_SNOWFLAKE).get().generate();
    }
}
