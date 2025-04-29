package com.tianji.learning.service;

import com.tianji.learning.domain.po.PointsBoard;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardVO;

import java.util.List;

/**
 * <p>
 * 学霸天梯榜 服务类
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
public interface IPointsBoardService extends IService<PointsBoard> {


    PointsBoardVO queryPointsBoardList(PointsBoardQuery query);

    List<PointsBoard> queryCurrentBoard(String key, Integer pageNo, Integer pageSize);


}
