package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.PointsMallItems;
import com.tianji.learning.domain.query.PointsItemsPageQuery;

public interface IPointsMallItemsService extends IService<PointsMallItems> {

    PageDTO<PointsMallItems> queryPointsItemsByPage(PointsItemsPageQuery query);
}