package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.PointsExchangeRecordDTO;
import com.tianji.learning.domain.po.PointsExchangeRecords;
import com.tianji.learning.domain.po.PointsMallItems;
import com.tianji.learning.domain.vo.PointsExchangeRecordsVO;
import com.tianji.learning.mapper.PointsExchangeRecordsMapper;
import com.tianji.learning.service.IPointsExchangeRecordsService;
import com.tianji.learning.service.IPointsMallItemsService;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsExchangeRecordsServiceImpl extends ServiceImpl<PointsExchangeRecordsMapper, PointsExchangeRecords> implements IPointsExchangeRecordsService {

    private final IPointsMallItemsService itemsService;
    private final IPointsRecordService pointsRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeItem(PointsExchangeRecordDTO dto) {
        // 查询商品信息
        PointsMallItems item = itemsService.getById(dto.getItemId());
        if (item == null || item.getStatus() != 1) {
            throw new BizIllegalException("商品不存在或已下架");
        }
        if (item.getStock() <= 0) {
            throw new BizIllegalException("商品库存不足");
        }
        Long userId = UserContext.getUser();
        // 检查用户积分是否足够
        Integer userPoints = pointsRecordService.getUserCurrentPoints(userId);
        if (userPoints < item.getPointsRequired()) {
            throw new BizIllegalException("积分不足，无法兑换");
        }
        // 扣减库存
        boolean success = itemsService.lambdaUpdate()
                .setSql("stock = stock - 1")
                .eq(PointsMallItems::getId, item.getId())
                .gt(PointsMallItems::getStock, 0)
                .update();
        if (!success) {
            throw new BizIllegalException("商品库存不足，兑换失败");
        }
        // 扣减用户积分（这里需要调用积分服务）
        pointsRecordService.consumePoints(userId, item.getPointsRequired(),
                "兑换商品：" + item.getItemName());
        // 记录兑换信息
        PointsExchangeRecords pointsExchangeRecords = new PointsExchangeRecords();
        pointsExchangeRecords.setItemId(dto.getItemId());
        pointsExchangeRecords.setUserId(userId);
        pointsExchangeRecords.setAddress(dto.getAddress());
        pointsExchangeRecords.setPhone(dto.getPhone());
        pointsExchangeRecords.setPointsUsed(item.getPointsRequired());
        pointsExchangeRecords.setStatus(0);
        pointsExchangeRecords.setCreateTime(LocalDateTime.now());
        save(pointsExchangeRecords);
    }

    @Override
    public void updateExchangeStatus(Long id, Byte status) {
        lambdaUpdate()
                .set(PointsExchangeRecords::getStatus, status)
                .eq(PointsExchangeRecords::getId, id)
                .update();
    }

    @Override
    public PageDTO<PointsExchangeRecordsVO> queryExchangeRecordsByUser(Long userId, PageQuery query) {
        Page<PointsExchangeRecords> page = lambdaQuery()
                .eq(PointsExchangeRecords::getUserId, userId)
                .orderByDesc(PointsExchangeRecords::getCreateTime)
                .page(query.toMpPage());
        List<PointsExchangeRecords> records = page.getRecords();
        List<PointsExchangeRecordsVO> voList= new ArrayList<>();
        for (PointsExchangeRecords record : records) {
            PointsExchangeRecordsVO vo = BeanUtils.copyProperties(record, PointsExchangeRecordsVO.class);
            vo.setItemName(itemsService.getById(vo.getItemId()).getItemName());
            vo.setItemUrl(itemsService.getById(vo.getItemId()).getImageUrl());
            voList.add(vo);
        }
        return PageDTO.of(page,voList);
    }

    @Override
    public PageDTO<PointsExchangeRecords> queryAllExchangeRecords(PageQuery query, Long itemId, Byte status) {
        LambdaQueryWrapper<PointsExchangeRecords> wrapper = new LambdaQueryWrapper<>();
        if (itemId != null) {
            wrapper.eq(PointsExchangeRecords::getItemId, itemId);
        }
        if (status != null) {
            wrapper.eq(PointsExchangeRecords::getStatus, status);
        }
        Page<PointsExchangeRecords> page = lambdaQuery()
                .orderByDesc(PointsExchangeRecords::getCreateTime)
                .page(query.toMpPage());
        return PageDTO.of(page);
    }

    @Override
    public PointsExchangeRecordsVO queryExchangeRecordById(Long id) {
        PointsExchangeRecords record = getById(id);
        PointsExchangeRecordsVO vo = BeanUtils.copyProperties(record, PointsExchangeRecordsVO.class);
        vo.setItemName(itemsService.getById(vo.getItemId()).getItemName());
        vo.setItemUrl(itemsService.getById(vo.getItemId()).getImageUrl());
        return vo;
    }

    @Override
    public void cancelExchangeStatus(Long id) {
        lambdaUpdate()
                .set(PointsExchangeRecords::getStatus, 3)
                .eq(PointsExchangeRecords::getId, id)
                .update();
    }
}