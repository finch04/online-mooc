package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.PointsExchangeRecordDTO;
import com.tianji.learning.domain.po.PointsExchangeRecords;
import com.tianji.learning.domain.vo.PointsExchangeRecordsVO;

public interface IPointsExchangeRecordsService extends IService<PointsExchangeRecords> {

    void exchangeItem(PointsExchangeRecordDTO dto);

    void updateExchangeStatus(Long id, Byte status);

    PageDTO<PointsExchangeRecordsVO> queryExchangeRecordsByUser(Long userId, PageQuery query);

    PageDTO<PointsExchangeRecords> queryAllExchangeRecords(PageQuery query, Long itemId, Byte status);

    PointsExchangeRecordsVO queryExchangeRecordById(Long id);

    void cancelExchangeStatus(Long id);
}