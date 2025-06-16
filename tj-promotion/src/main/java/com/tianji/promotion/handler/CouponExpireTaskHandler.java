package com.tianji.promotion.handler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.service.IUserCouponService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponExpireTaskHandler {

    private final IUserCouponService userCouponService;

    @XxlJob("couponExpireJobHandler")
    public void handleCouponExpireJob(){
        // 1.获取分片信息，作为页码，每页最多查询 20条
        int index = XxlJobHelper.getShardIndex() + 1;
        int size = Integer.parseInt(XxlJobHelper.getJobParam());
        // 2.查询<过期的优惠券
        Page<UserCoupon> page = userCouponService.lambdaQuery()
                .eq(UserCoupon::getStatus, UserCouponStatus.UNUSED)
                .le(UserCoupon::getTermEndTime, LocalDateTime.now())
                .page(new Page<>(index, size));
        // 3.发放优惠券
        List<UserCoupon> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return;
        }
        records.forEach(record -> record.setStatus(UserCouponStatus.EXPIRED));
        userCouponService.updateBatchById(records);
    }
}