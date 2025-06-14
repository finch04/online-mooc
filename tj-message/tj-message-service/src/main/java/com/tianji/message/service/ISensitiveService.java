package com.tianji.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.po.Sensitive;
import com.tianji.message.domain.query.SensitiveQuery;

import java.util.List;

public interface ISensitiveService extends IService<Sensitive> {
    PageDTO<Sensitive> getAllSensitiveWords(SensitiveQuery query);

    boolean saveSensitive(Sensitive sensitive);

    boolean updateSensitive(Sensitive sensitive);

    boolean deleteSensitive(Long id);
}
