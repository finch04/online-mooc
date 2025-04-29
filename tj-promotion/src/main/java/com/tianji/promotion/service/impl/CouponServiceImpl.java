package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.cache.CategoryCache;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.*;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponDetailVO;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponScopeVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ObtainType;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tianji.promotion.enums.CouponStatus.*;

/**
 * <p>
 * 优惠券的规则信息 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    private final ICouponScopeService couponScopeService;
    private final CategoryCache categoryCache;//获取分类信息
    private final StringRedisTemplate redisTemplate;
    private final IExchangeCodeService exchangeCodeService;
    private final IUserCouponService userCouponService;


    //新增优惠券
    @Override
    @Transactional
    public void saveCoupon(CouponFormDTO dto) {
        //dto转po
        Coupon coupon = BeanUtils.copyBean(dto, Coupon.class);
        this.save(coupon);
        //判断优惠券是否限定了范围
        if(!coupon.getSpecific()){
          return;//说明未限定其范围
        }
        //如果限制了范围，则需要检查优惠券的scope属性是否为空
        List<Long> scopes = dto.getScopes();
        if(CollUtils.isEmpty(scopes)){
            throw new BizIllegalException("分类id不能为空！");
        }

        //保存优惠券的限定范围 coupon_scope 批量新增
//        List<CouponScope> csList=new ArrayList<>();
//        for (Long scope : scopes) {
//            CouponScope couponScope =new CouponScope();
//            couponScope.setCouponId(coupon.getId());
//            couponScope.setBizId(scope);
//            couponScope.setType(1);
//            csList.add(couponScope);
//        }

        //stream流写法
        List<CouponScope> csList=scopes.stream().map(aLong -> new CouponScope().setCouponId(coupon.getId()).setBizId(aLong).setType(1)).collect(Collectors.toList());
        couponScopeService.saveBatch(csList);
    }

    //分页查询优惠券
    @Override
    public PageDTO<CouponPageVO> queryCouponPage(CouponQuery query) {
        //分页条件查询优惠券表
        Page<Coupon> page = this.lambdaQuery()
                .eq(query.getType() != null, Coupon::getDiscountType, query.getType())
                .eq(query.getStatus() != null, Coupon::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getName()), Coupon::getName, query.getName())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<Coupon> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //封装vo返回
        List<CouponPageVO> voList =BeanUtils.copyList(records, CouponPageVO.class);
        return PageDTO.of(page,voList);
    }

    //发放优惠券
    @Override
    public void issueCoupon(Long id,CouponIssueFormDTO dto) {
        log.debug("发放优惠券  线程名字: {}",Thread.currentThread().getName());
        //校验
        if(id == null || !id.equals(dto.getId())){
            throw new BadRequestException("非法参数");
        }
        //校验优惠券id是否存在
        Coupon coupon = this.getById(id);
        if(coupon==null){
            throw new BadRequestException("优惠券不存在");
        }
        //校验优惠券状态，只有待发放和暂停状态才可以发放
        if(coupon.getStatus()!= PAUSE && coupon.getStatus()!=CouponStatus.DRAFT){
            throw new BizIllegalException("只有待发放和暂停中的优惠券才可以发放");
        }
        LocalDateTime now =LocalDateTime.now();
        boolean isBeginIssue = dto.getIssueBeginTime()==null || !dto.getIssueBeginTime().isAfter(now);//该变量代表优惠券是否可立即发放
        //修改优惠券的领取开始和结束日期，使用有效期开始和结束日 天数 状态

        //方法1
//        if(isBeginIssue){
//            coupon.setIssueBeginTime(dto.getIssueBeginTime()==null?now:dto.getIssueBeginTime());
//            coupon.setIssueEndTime(dto.getIssueEndTime());
//            coupon.setStatus(CouponStatus.ISSUING);
//            coupon.setTermDays(dto.getTermDays());
//            coupon.setTermBeginTime(dto.getTermBeginTime());
//            coupon.setTermEndTime(dto.getTermEndTime());
//        }else{
//            coupon.setIssueBeginTime(dto.getIssueBeginTime());
//            coupon.setIssueEndTime(dto.getIssueEndTime());
//            coupon.setStatus(CouponStatus.UN_ISSUE);
//            coupon.setTermDays(dto.getTermDays());
//            coupon.setTermBeginTime(dto.getTermBeginTime());
//            coupon.setTermEndTime(dto.getTermEndTime());
//        }
//        this.updateById(coupon);
        //方法2
        Coupon tmp = BeanUtils.copyBean(dto, Coupon.class);
        if(isBeginIssue){
            tmp.setIssueBeginTime(now);
            tmp.setStatus(ISSUING);
        }else{
            tmp.setStatus(UN_ISSUE);
        }
        this.updateById(tmp);

        //如果优惠券是立刻发放  应该将优惠券id 优惠券剩余数量 优惠券限领数 领取的开始结束时间  采用hash 存入redis
        if(isBeginIssue){
            String key =PromotionConstants.COUPON_CACHE_KEY_PREFIX+id;
//            redisTemplate.opsForHash().put(key,"issueBeginTime",String.valueOf(DateUtils.toEpochMilli(now)));
//            redisTemplate.opsForHash().put(key,"issueEndTime",String.valueOf(DateUtils.toEpochMilli(dto.getIssueEndTime())));
//            redisTemplate.opsForHash().put(key,"totalNum",String.valueOf(coupon.getTotalNum()));
//            redisTemplate.opsForHash().put(key,"userLimit",String.valueOf(coupon.getUserLimit()));

            Map<String,String> map =new HashMap<>();
            map.put("issueBeginTime",String.valueOf(DateUtils.toEpochMilli(now)));
            map.put("issueEndTime",String.valueOf(DateUtils.toEpochMilli(dto.getIssueEndTime())));
            map.put("totalNum",String.valueOf(coupon.getTotalNum()));
            map.put("userLimit",String.valueOf(coupon.getUserLimit()));
            redisTemplate.opsForHash().putAll(key,map);

        }


        //如果优惠券的生成方式为指定发放，且优惠券之前的状态是待发放需要生成兑换码
        if(coupon.getObtainWay()== ObtainType.ISSUE && coupon.getStatus()== CouponStatus.DRAFT){
            coupon.setIssueEndTime(tmp.getIssueEndTime());//兑换码兑换的截止时间 就是优惠券领取的截止时间
            exchangeCodeService.asyncGenerateExchangeCode(coupon);//异步生成兑换码
        }

    }

    //修改优惠券
    @Override
    public void modifyCoupon(Long id,CouponFormDTO dto) {
        //校验
        if(id == null || !id.equals(dto.getId())){
            throw new BadRequestException("非法参数");
        }
        //校验优惠券id是否存在
        Coupon couponOld = this.getById(id);
        if(couponOld==null){
            throw new BadRequestException("优惠券不存在");
        }
        //dto转po
        Coupon coupon = BeanUtils.copyBean(dto, Coupon.class);
        this.updateById(coupon);


        //判断优惠券是否限定了范围
        if(!couponOld.getSpecific()){
            //说明旧的未限定其范围
            if(!coupon.getSpecific()) {
                //说明新的也未限定范围
                return ;
            }else{
                //说明新的限定了范围
                List<Long> scopes = dto.getScopes();
                if(CollUtils.isEmpty(scopes)){
                    throw new BizIllegalException("分类id不能为空！");
                }
                List<CouponScope> csList=scopes.stream().map(aLong -> new CouponScope().setCouponId(coupon.getId()).setBizId(aLong).setType(1)).collect(Collectors.toList());
                couponScopeService.saveBatch(csList);
            }
        } else{
            //说明旧的限定了范围
            if(!coupon.getSpecific()){
                //说明新的未限定范围
                couponScopeService.remove(new LambdaQueryWrapper<CouponScope>().eq(CouponScope::getCouponId, id));
            }else{
                //说明新的限定了范围
                List<Long> scopes = dto.getScopes();
                if(CollUtils.isEmpty(scopes)){
                    throw new BizIllegalException("分类id不能为空！");
                }
                //先将旧的范围删除
                couponScopeService.remove(new LambdaQueryWrapper<CouponScope>().eq(CouponScope::getCouponId, id));
                //stream流写法
                List<CouponScope> csList=scopes.stream().map(aLong -> new CouponScope().setCouponId(coupon.getId()).setBizId(aLong).setType(1)).collect(Collectors.toList());
                couponScopeService.saveBatch(csList);

            }

        }

    }

    //按id查询优惠券
    @Override
    public CouponDetailVO queryCouponById(Long id) {
        // 1.查询优惠券
        Coupon coupon = getById(id);
        // 2.转换VO
        CouponDetailVO vo = BeanUtils.copyBean(coupon, CouponDetailVO.class);
        if (vo == null || !coupon.getSpecific()) {
            // 数据不存在，或者没有限定范围，直接结束
            return vo;
        }
        // 3.查询限定范围
        List<CouponScope> scopes = couponScopeService.lambdaQuery().eq(CouponScope::getCouponId, id).list();
        if (CollUtils.isEmpty(scopes)) {
            return vo;
        }
        List<CouponScopeVO> scopeVOS = scopes.stream()
                .map(CouponScope::getBizId)
                .map(cateId -> new CouponScopeVO(cateId, categoryCache.getNameByLv3Id(cateId)))
                .collect(Collectors.toList());
        vo.setScopes(scopeVOS);
        return vo;
    }

    //暂停发放
    @Override
    public void pauseIssue(Long id) {
        // 1.查询旧优惠券
        Coupon coupon = getById(id);
        if (coupon == null) {
            throw new BadRequestException("优惠券不存在");
        }

        // 2.当前券状态必须是未开始或进行中
        CouponStatus status = coupon.getStatus();
        if (status != UN_ISSUE && status != ISSUING) {
            // 状态错误，直接结束
            return;
        }

        // 3.更新状态
        boolean success = lambdaUpdate()
                .set(Coupon::getStatus, PAUSE)
                .eq(Coupon::getId, id)
                .in(Coupon::getStatus, UN_ISSUE, ISSUING)
                .update();
        if (!success) {
            // 可能是重复更新，结束
            log.error("重复暂停优惠券");
        }

        //删除缓存
        redisTemplate.delete(PromotionConstants.COUPON_CACHE_KEY_PREFIX + id);
    }

    //删除优惠券
    @Override
    public void deleteById(Long id) {
        Coupon coupon = this.getById(id);
        if (coupon == null || coupon.getStatus() != DRAFT) {
            throw new BadRequestException("优惠券不存在或者优惠券正在使用中");
        }
        // 2.删除优惠券
        boolean success = remove(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getId, id)
                .eq(Coupon::getStatus, DRAFT)
        );
        if (!success) {
            throw new BadRequestException("优惠券不存在或者优惠券正在使用中");
        }
        // 3.删除优惠券对应限定范围
        if(!coupon.getSpecific()){
            return;
        }
        couponScopeService.remove(new LambdaQueryWrapper<CouponScope>().eq(CouponScope::getCouponId, id));
    }

    //定时发放优惠券
    @Override
    public void beginIssueBatch(List<Coupon> coupons) {
        // 1.更新券状态
        for (Coupon c : coupons) {
            c.setStatus(CouponStatus.ISSUING);
        }
        updateBatchById(coupons);
        // 2.批量缓存
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection src = (StringRedisConnection) connection;
            for (Coupon coupon : coupons) {
                // 2.1.组织数据
                Map<String, String> map = new HashMap<>(4);
                map.put("issueBeginTime", String.valueOf(DateUtils.toEpochMilli(coupon.getIssueBeginTime())));
                map.put("issueEndTime", String.valueOf(DateUtils.toEpochMilli(coupon.getIssueEndTime())));
                map.put("totalNum", String.valueOf(coupon.getTotalNum()));
                map.put("userLimit", String.valueOf(coupon.getUserLimit()));
                // 2.2.写缓存
                src.hMSet(PromotionConstants.COUPON_CACHE_KEY_PREFIX + coupon.getId(), map);
            }
            return null;
        });
    }

    //查询正在发放的优惠券
    @Override
    public List<CouponVO> queryIssuingCoupons() {
        List<Coupon> couponList = this.lambdaQuery()
                .eq(Coupon::getStatus, ISSUING)
                .eq(Coupon::getObtainWay,ObtainType.PUBLIC)
                .list();
        if(CollUtils.isEmpty(couponList)){
            return CollUtils.emptyList();
        }
        //查询用户券表 条件当前用户，发放的优惠券id
        Set<Long> couponIds = couponList.stream().map(Coupon::getId).collect(Collectors.toSet());
        //当前用户针对正在发放的优惠券的领取记录
        List<UserCoupon> list = userCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, UserContext.getUser())
                .in(UserCoupon::getCouponId, couponIds)
                .list();
//        Map<Long,Long> issueMap =new HashMap<>();//代表当前用户针对每一个券的领取数量
//        for (UserCoupon userCoupon : list) {
//            Long num =issueMap.get(userCoupon.getCouponId());
//            if(num==null){
//                issueMap.put(userCoupon.getCouponId(),1L);
//            }else{
//                issueMap.put(userCoupon.getCouponId(),Long.valueOf(num.intValue()+1));
//            }
//        }
        //统计当前用户 针对每一个券的已领数量
        Map<Long, Long> issueMap = list.stream().collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        //统计当前用户 针对每一个券的已领且未使用数量
        Map<Long, Long> unuseMap = list.stream()
                .filter(c -> c.getStatus() == UserCouponStatus.UNUSED)
                .collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));

        //po转vo
        List<CouponVO> voList=new ArrayList<>();
        for (Coupon coupon : couponList) {
            CouponVO vo =BeanUtils.copyBean(coupon, CouponVO.class);
            //优惠券还有剩余 issue_num < total_num 且（统计用户卷表user_coupon 取出当前用户已领数量<user_limit）
            Long issNum=issueMap.getOrDefault(coupon.getId(),0L);
            boolean avaliable = coupon.getIssueNum()<coupon.getTotalNum() && issNum.intValue()<coupon.getUserLimit();
            vo.setAvailable(avaliable);//是否可以领取

            //统计用户卷表 user_coupon取出当前用户已经未使用的卷数量
            boolean received= unuseMap.getOrDefault(coupon.getId(),0L)>0;
            vo.setReceived(received);//是否可以使用
            voList.add(vo);
        }
        return voList;
    }
}
