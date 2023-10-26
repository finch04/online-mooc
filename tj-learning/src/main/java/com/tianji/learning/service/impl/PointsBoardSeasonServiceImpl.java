package com.tianji.learning.service.impl;

import com.tianji.common.utils.DateUtils;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;
import com.tianji.learning.mapper.PointsBoardSeasonMapper;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason> implements IPointsBoardSeasonService {

    //查询赛季列表
    @Override
    public List<PointsBoardSeasonVO> querySeasonByTime() {
        List<PointsBoardSeasonVO> voList=new ArrayList<>();
        List<PointsBoardSeason> list = this.lambdaQuery().list();
        for (PointsBoardSeason pointsBoardSeason : list) {
            PointsBoardSeasonVO vo=new PointsBoardSeasonVO();
            vo.setName(pointsBoardSeason.getName());
            vo.setBeginTime(pointsBoardSeason.getBeginTime());
            vo.setEndTime(pointsBoardSeason.getEndTime());
            vo.setId(pointsBoardSeason.getId());
            voList.add(vo);
        }
        return voList;
    }
}

