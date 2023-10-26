package com.tianji.learning.service;

import com.tianji.learning.domain.vo.SignResultVO;

public interface ISignRecordsService {
    SignResultVO addSignRecords();

    Byte[] querySignRecords();
}
