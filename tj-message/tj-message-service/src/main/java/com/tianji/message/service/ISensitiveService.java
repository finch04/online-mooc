package com.tianji.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.message.domain.po.Sensitive;

import java.util.List;

public interface ISensitiveService extends IService<Sensitive> {
    List<Sensitive> getAllSensitiveWords();

    boolean saveSensitive(Sensitive sensitive);

    boolean updateSensitive(Sensitive sensitive);

    boolean deleteSensitive(Integer id);
}
