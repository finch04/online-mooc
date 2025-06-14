package com.tianji.data.service;


import com.tianji.api.dto.data.CourseDataVO;
import com.tianji.api.dto.data.OrderDataVO;
import com.tianji.data.model.dto.BoardDataSetDTO;
import com.tianji.data.model.vo.EchartsVO;

import java.util.List;

/**
 * @ClassName BoardService
 * @Author wusongsong
 * @Date 2022/10/10 16:30
 * @Version
 **/
public interface BoardService {

    /**
     * 看板数据获取
     *
     * @param types 数据类型
     * @return
     */
    EchartsVO boardData(List<Integer> types);

    /**
     * 设置看板数据
     *
     * @param boardDataSetDTO
     */
    void setBoardData(BoardDataSetDTO boardDataSetDTO);

    /**
     * 查看课程看板数据
     * @return
     */
    CourseDataVO courseBoardData();


    /**
     * 查看订单看板数据
     * @return
     */
    OrderDataVO orderBoardData();

    /**
     * 更新订单数据
     * @param orderData
     */
    void updateOrderData(OrderDataVO orderData);

    /**
     * 更新课程数据
     * @param courseData
     */
    void updateCourseData(CourseDataVO courseData);
}