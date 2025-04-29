package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;
import com.tianji.promotion.mapper.ExchangeCodeMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.tianji.promotion.constants.PromotionConstants.COUPON_RANGE_KEY;

/**
 * <p>
 * 兑换码 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode> implements IExchangeCodeService {

    private final StringRedisTemplate redisTemplate;
    
    //异步生成兑换码
    @Override
    @Async("generateExchangeCodeExecutor")//使用自定义线程池中的generateExchangeCodeExecutor线程异步执行
    public void asyncGenerateExchangeCode(Coupon coupon) {
        log.debug("生成兑换码  线程池：{}", Thread.currentThread().getName());
        Integer totalNum = coupon.getTotalNum();
        //方法1 循环兑换码总数 循环中单个获取自增id incr
        //方法2 先调用incrby 得到自增id最大值 然后再循环生成兑换码
        //生成自增id 借助redis incrby
        Long increment = redisTemplate.opsForValue().increment(PromotionConstants.COUPON_CODE_SERIAL_KEY ,totalNum);
        if(increment==null){
            return;
        }
        int maxSerialNum = increment.intValue();//本地自增id最大值
        int begin=maxSerialNum-totalNum+1;//自增id开始值
        //调用工具类生成兑换码
        List<ExchangeCode> list =new ArrayList<>();
        for(int serialNum =begin;serialNum<=maxSerialNum;serialNum++){
            String code = CodeUtil.generateCode(serialNum, coupon.getId());//参数1为自增id值，参数2为优惠券id，内部计算出0~15的数字然后找对应的密钥
            ExchangeCode exchangeCode=new ExchangeCode();
            exchangeCode.setId(serialNum);//兑换码id
            exchangeCode.setCode(code);
            exchangeCode.setExpiredTime(coupon.getIssueEndTime());//兑换码兑换的截止时间就是优惠券领取的截止时间
            exchangeCode.setExchangeTargetId(coupon.getId());
            list.add(exchangeCode);
        }

        //将兑换码保存db exchange_code 批量保存
        this.saveBatch(list);
        //写入Redis缓存，member：couponId，score：兑换码的最大序列号 (本次生成兑换码，可省略)
        redisTemplate.opsForZSet().add(COUPON_RANGE_KEY, coupon.getId().toString(), maxSerialNum);

    }

    //分页查询兑换码
    @Override
    public PageDTO<ExchangeCodeVO> queryCodePage(CodeQuery query) {
        // 1.分页查询兑换码
        Page<ExchangeCode> page = lambdaQuery()
                .eq(ExchangeCode::getStatus, query.getStatus())
                .eq(ExchangeCode::getExchangeTargetId, query.getCouponId())
                .page(query.toMpPage());
        // 2.返回数据
        return PageDTO.of(page, c -> new ExchangeCodeVO(c.getId(), c.getCode()));
    }

    //更新redis中兑换码是否被兑换
    @Override
    public boolean updateExchangeCodeMark(long serialNum, boolean flag) {
        String key =PromotionConstants.COUPON_CODE_MAP_KEY;
        //修改兑换码的自增id  对应的offset的值
        Boolean aBoolean = redisTemplate.opsForValue().setBit(key, serialNum, flag);

        return aBoolean!=null&&aBoolean;
    }

    //找到优惠券的最大序列号
    @Override
    public Long exchangeTargetId(long serialNum) {
        // 1.查询score值比当前序列号大的第一个优惠券
        Set<String> results = redisTemplate.opsForZSet().rangeByScore(
                COUPON_RANGE_KEY, serialNum, serialNum + 5000, 0L, 1L);
        if (CollUtils.isEmpty(results)) {
            return null;
        }
        // 2.数据转换
        String next = results.iterator().next();
        return Long.parseLong(next);
    }
}
