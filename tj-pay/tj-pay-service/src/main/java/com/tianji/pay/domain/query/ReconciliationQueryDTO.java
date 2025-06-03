package com.tianji.pay.domain.query;

import com.tianji.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class ReconciliationQueryDTO extends PageQuery {
    private Integer status; // 可选，对账状态
}