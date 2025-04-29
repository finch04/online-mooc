/*
package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.domain.query.UserCouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

*/
/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-29
 *//*

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {

    private final CouponMapper couponMapper;
    private final IExchangeCodeService exchangeCodeService;

    //领取优惠券
    @Override
//    @Transactional
    public void receiveCoupon(Long id) {
        //校验
        if(id==null){
            throw new BadRequestException("非法参数");
        }
        Coupon coupon=couponMapper.selectById(id);
        if(coupon==null){
            throw new BadRequestException("优惠券不存在");
        }
        if(coupon.getStatus()!= CouponStatus.ISSUING){
            throw new BadRequestException("该优惠券状态不是正在发放");
        }
        LocalDateTime now =LocalDateTime.now();
        if(now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
            throw new BadRequestException("该优惠券已过期或未开始发放");
        }
        if(coupon.getTotalNum()<=0 || coupon.getIssueNum()>=coupon.getTotalNum()){
            throw new BadRequestException("该优惠券库存不足");
        }
        Long userId = UserContext.getUser();
        //获取当前用户 对该优惠券已领数量
*/
/*        Integer count =this.lambdaQuery()
                .eq(UserCoupon::getUserId,userId)
                .eq(UserCoupon::getCouponId,id)
                .count();
        if(count != null && count>=coupon.getUserLimit()){
            throw new BadRequestException("已达到领取上限");
        }
        //优惠券已发放数量+1
        //此处可能会造成超卖问题 不是原子操作
//        coupon.setIssueNum(coupon.getIssueNum()+1);
//        couponMapper.updateById(coupon);
        couponMapper.incrIssueNum(id);//先采用这种方式 todo 后期考虑并发控制*//*


        //生成用户券
//        synchronized (userId.toString().intern()){
//            checkAndCreateUserCoupon(userId,coupon,null);
//        }

        synchronized (userId.toString().intern()){
            //从aop上下文中 获取当前类的代理对象
            IUserCouponService userCouponServiceProxy= (IUserCouponService) AopContext.currentProxy();
//            checkAndCreateUserCoupon(userId,coupon,null);
            userCouponServiceProxy.checkAndCreateUserCoupon(userId,coupon,null);//这种写法调用代理对象的方法，方法有事务的
        }

//        saveUserCoupon(userId,coupon);

        
    }

    //保存用户券
    private void saveUserCoupon(Long userId, Coupon coupon) {
        UserCoupon userCoupon =new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(coupon.getId());
        LocalDateTime termBeginTime = coupon.getTermBeginTime();
        LocalDateTime termEndTime = coupon.getTermEndTime();
        if(termBeginTime==null && termEndTime==null){
            termBeginTime=LocalDateTime.now();
            termEndTime=termBeginTime.plusDays(coupon.getTermDays());
        }
        userCoupon.setTermBeginTime(termBeginTime);
        userCoupon.setTermEndTime(termEndTime);
        this.save(userCoupon);

    }

    //兑换码兑换优惠券
    @Override
    public void exchangeCoupon(String code) {
        //校验code是否为空
        if(code == null) {
            throw new BadRequestException("非法参数");
        }
        //解析兑换码得到自增id
        long serialNum = CodeUtil.parseCode(code);
        log.debug("自增id：{}",serialNum);
        //判断兑换码是否已兑换  采用redis的bitmap结构 setbit key offset 1 如果方法返回true 代表兑换码已经兑换
        boolean result = exchangeCodeService.updateExchangeCodeMark(serialNum,true);
        if(result){
            throw new BizIllegalException("兑换码已被使用！");
        }
        try{
            //判断兑换码是否存在 根据自增id查询 主键查询
            ExchangeCode exchangeCode = exchangeCodeService.getById(serialNum);
            if(exchangeCode==null){
                throw new BizIllegalException("兑换码不存在!");
            }
            //判断是否过期
            LocalDateTime now =LocalDateTime.now();
            LocalDateTime expireTime =exchangeCode.getExpiredTime();
            if(now.isAfter(expireTime)){
                throw new BizIllegalException("兑换码已过期");
            }

            //校验并生成用户券
            Long userId =UserContext.getUser();
            //查询优惠券
            Coupon coupon = couponMapper.selectById(exchangeCode.getExchangeTargetId());
            if(coupon==null){
                throw new BizIllegalException("对应优惠券不存在！");
            }
            //判断是否超出限领数量 优惠券已发放数量+1 生成用户券 更新兑换码状态
            checkAndCreateUserCoupon(userId,coupon,serialNum);

        }catch(Exception e){
            //将兑换码状态重置  报错mysql回滚 但别忘需要把redis修改
            exchangeCodeService.updateExchangeCodeMark(serialNum,false);
            throw e ;
        }

    }

    @Override
    @Transactional
    public  void checkAndCreateUserCoupon(Long userId, Coupon coupon,Long serialNum) {
        //userId.toString().intern()强制从常量值取字符串
//        synchronized (userId.toString().intern()) {
            //获取当前用户 对该优惠券已领数量
            Integer count =this.lambdaQuery()
                    .eq(UserCoupon::getUserId,userId)
                    .eq(UserCoupon::getCouponId,coupon.getId())
                    .count();
            if(count != null && count>=coupon.getUserLimit()){
                throw new BadRequestException("已达到领取上限");
            }
            //优惠券已发放数量+1
            //此处可能会造成超卖问题 不是原子操作
            couponMapper.incrIssueNum(coupon.getId());//先采用这种方式 todo 后期考虑并发控制

            //生成用户券
            saveUserCoupon(userId,coupon);

            //更新兑换码状态
            if(serialNum!=null){
                exchangeCodeService.lambdaUpdate()
                        .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                        .set(ExchangeCode::getUserId,userId)
                        .eq(ExchangeCode::getId,serialNum)
                        .update();
            }
//        }
    }

    //分页查询我的优惠券
    @Override
    public PageDTO<CouponVO> queryMyCouponPage(UserCouponQuery query) {
        //获取当前用户id
        Long userId =UserContext.getUser();

        Page<UserCoupon> page = this.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, query.getStatus())
                .page(query.toMpPage(new OrderItem("term_end_time", true)));
        List<UserCoupon> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }

        // 获取优惠券详细信息
        // 获取用户券关联的优惠券id
        Set<Long> couponIds = records.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        // 查询
        List<Coupon> coupons = couponMapper.selectBatchIds(couponIds);

        // 封装VO
        return PageDTO.of(page, BeanUtils.copyList(coupons, CouponVO.class));
    }
}
*/
