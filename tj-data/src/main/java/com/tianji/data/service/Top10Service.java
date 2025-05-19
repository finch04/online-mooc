package com.tianji.data.service;


import com.tianji.data.model.dto.Top10DataSetDTO;
import com.tianji.data.model.vo.Top10DataVO;

/**
 * @author fsq
 * @since 2025-5-18 19:06:26
 **/
public interface Top10Service {

    /**
     * 获取top数据
     *
     * @return
     */
    Top10DataVO getTop10Data();

    /**
     * top 10数据设置
     * @param top10DataSetDTO
     */
    void setTop10Data(Top10DataSetDTO top10DataSetDTO);
}