package com.tianji.promotion.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.NumberUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.discount.Discount;
import com.tianji.promotion.discount.DiscountStrategy;
import com.tianji.promotion.domain.dto.CouponDiscountDTO;
import com.tianji.promotion.domain.dto.OrderCouponDTO;
import com.tianji.promotion.domain.dto.OrderCourseDTO;
import com.tianji.promotion.domain.dto.UserCouponDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.domain.query.UserCouponQuery;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.tianji.promotion.constants.PromotionConstants.COUPON_CODE_MAP_KEY;
import static com.tianji.promotion.constants.PromotionConstants.COUPON_RANGE_KEY;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserCouponLuaServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {

    private final CouponMapper couponMapper;
    private final IExchangeCodeService codeService;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitMqHelper mqHelper;
    private final ICouponScopeService scopeService;
    private final Executor discountSolutionExecutor;
    private final UserCouponMapper userCouponMapper;

    private static final RedisScript<Long> RECEIVE_COUPON_SCRIPT ;

    private static final RedisScript<String> EXCHANGE_COUPON_SCRIPT ;

    static {
        RECEIVE_COUPON_SCRIPT = RedisScript.of(new ClassPathResource("lua/receive_coupon.lua"), Long.class);
        EXCHANGE_COUPON_SCRIPT = RedisScript.of(new ClassPathResource("lua/exchange_coupon.lua"), String.class);
    }

    //领取优惠券
    // 使用LUA脚本后无需加锁也是线程安全的
    @Override
    public void receiveCoupon(Long couponId) {
        // 1.执行LUA脚本，判断结果
        // 1.1.准备参数
        String key1 = PromotionConstants.COUPON_CACHE_KEY_PREFIX + couponId;
        String key2 = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + couponId;
        Long userId = UserContext.getUser();
        // 1.2.执行脚本
        Long r = redisTemplate.execute(RECEIVE_COUPON_SCRIPT, List.of(key1, key2), userId.toString());
        int result = NumberUtils.null2Zero(r).intValue();
        if (result != 0) {
            // 结果大于0，说明出现异常
            throw new BizIllegalException(PromotionConstants.RECEIVE_COUPON_ERROR_MSG[result - 1]);
        }
        // 2.发送MQ消息
        UserCouponDTO uc = new UserCouponDTO();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        mqHelper.send(MqConstants.Exchange.PROMOTION_EXCHANGE, MqConstants.Key.COUPON_RECEIVE, uc);
    }

    // 使用LUA脚本后无需加锁也是线程安全的
    @Override
    public void exchangeCoupon(String code) {
        // 1.校验并解析兑换码
        long serialNum = CodeUtil.parseCode(code);
        // 2.执行LUA脚本
        Long userId = UserContext.getUser();
        String result = redisTemplate.execute(
                EXCHANGE_COUPON_SCRIPT,
                List.of(COUPON_CODE_MAP_KEY, COUPON_RANGE_KEY),
                String.valueOf(serialNum), String.valueOf(serialNum + 5000), userId.toString());
        long r = NumberUtils.parseLong(result);
        if (r < 10) {
            // 异常结果应该是在1~5之间
            throw new BizIllegalException(PromotionConstants.EXCHANGE_COUPON_ERROR_MSG[(int) (r - 1)]);
        }
        // 3.发送MQ消息通知
        UserCouponDTO uc = new UserCouponDTO();
        uc.setUserId(userId);
        uc.setCouponId(r);
        uc.setSerialNum((int) serialNum);
        mqHelper.send(MqConstants.Exchange.PROMOTION_EXCHANGE, MqConstants.Key.COUPON_RECEIVE, uc);
    }

    @Transactional
    @Override
    public void checkAndCreateUserCouponNew(UserCouponDTO uc) {
        // 1.查询优惠券
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在！");
        }
        // 2.更新优惠券的已经发放的数量 + 1
        int r = couponMapper.incrIssueNum(coupon.getId());
        if (r == 0) {
            throw new BizIllegalException("优惠券库存不足！");
        }
        // 3.新增一个用户券
        saveUserCoupon(uc.getUserId(), coupon);

        // 4.更新兑换码状态
        if (uc.getSerialNum() != null) {
            codeService.lambdaUpdate()
                    .set(ExchangeCode::getUserId, uc.getUserId())
                    .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                    .eq(ExchangeCode::getId, uc.getSerialNum())
                    .update();
        }
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

    @Override
    public List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courses) {
        //查询当前用户可用的优惠券 coupon和user_coupon表 条件 userid status=1 查哪些字段：优惠券规则 优惠券id 用户券id
        List<Coupon> coupons=getBaseMapper().queryMyCoupons(UserContext.getUser());
        if(CollUtils.isEmpty(coupons)){
            return CollUtils.emptyList();
        }
        log.debug("用户优惠券有：{}张",coupons.size());
        for (Coupon coupon : coupons) {
            log.debug("优惠券：{}，  {}",DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon),coupon);
        }

        //初筛
        //计算订单的总金额  对course和price累加
        int totalNum = courses.stream().mapToInt(OrderCourseDTO::getPrice).sum();
        log.debug("订单总金额：{}元",totalNum);
        //校验优惠券是否可用
//        List<Coupon> avaliableCoupons = new ArrayList<>();
//        for (Coupon coupon : coupons) {
//            boolean flag = DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(totalNum, coupon);
//            if(flag){
//                avaliableCoupons.add(coupon);
//            }
//        }
        List<Coupon> availableCoupons=coupons.stream().filter(coupon -> DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(totalNum, coupon)).collect(Collectors.toList());
        if(CollUtils.isEmpty(availableCoupons)){
            return CollUtils.emptyList();
        }
        log.debug("初筛后 用户优惠券有：{}张",availableCoupons.size());
        for (Coupon coupon : availableCoupons) {
            log.debug("优惠券：{}，  {}",DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon),coupon);
        }
        //细筛（考虑优惠券限定范围 排列组合）
        Map<Coupon,List<OrderCourseDTO>> avaMap= findAvailableCoupons(availableCoupons,courses);
        if(avaMap.isEmpty()){
            return CollUtils.emptyList();
        }
        Set<Map.Entry<Coupon, List<OrderCourseDTO>>> entries = avaMap.entrySet();
        for (Map.Entry<Coupon, List<OrderCourseDTO>> entry : entries) {
            log.debug("细筛之后的优惠券：{} {}"
                    ,DiscountStrategy.getDiscount(entry.getKey().getDiscountType()).getRule(entry.getKey())
                    ,entry.getKey());
            List<OrderCourseDTO> value = entry.getValue();
            for (OrderCourseDTO courseDTO : value) {
                log.debug("可用课程：{}",courseDTO);
            }
        }

        availableCoupons = new ArrayList<>(avaMap.keySet());
        log.debug("经过细筛后的优惠券个数:{}",availableCoupons.size());
        for (Coupon coupon : availableCoupons) {
            log.debug("优惠券：{}  {}",DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon),coupon);
        }

        List<List<Coupon>> solutions = PermuteUtil.permute(availableCoupons);
        for (Coupon coupon: availableCoupons) {
            solutions.add(List.of(coupon));//添加单券到方案中
        }
        log.debug("排列组合");
        for (List<Coupon> solution : solutions) {
            List<Long> cids = solution.stream().map(Coupon::getId).collect(Collectors.toList());
            log.debug("{}",cids);
        }
        // 计算每一种组合的优惠明细
//        log.debug("开始计算每一种组合的优惠明细");
//        List<CouponDiscountDTO> dtos =new ArrayList<>();
//        for (List<Coupon> solution : solutions) {
//            CouponDiscountDTO dto = calculateSolutionDiscount(avaMap,courses,solution);
//            log.debug("方案最终优惠{}  方案中优惠券使用了{} 规则{}",dto.getDiscountAmount(),dto.getIds(),dto.getRules());
//            dtos.add(dto);
//        }
        //使用多线程改造 并行计算每一组合的优惠明细
        log.debug("多线程--开始计算每一种组合的优惠明细");
//        List<CouponDiscountDTO> dtos =new ArrayList<>();//线程不安全的
        List<CouponDiscountDTO> dtos = Collections.synchronizedList(new ArrayList<>(solutions.size()));
        CountDownLatch latch = new CountDownLatch(solutions.size());
        for (List<Coupon> solution : solutions) {
            CompletableFuture.supplyAsync(new Supplier<CouponDiscountDTO>() {
                @Override
                public CouponDiscountDTO get() {
                    CouponDiscountDTO dto = calculateSolutionDiscount(avaMap,courses,solution);
                    // 创建优惠券ID到creator的映射
                    Map<Long, Long> couponIdToCreatorMap = new HashMap<>();
                    for (Coupon coupon : solution) {
                        couponIdToCreatorMap.put(coupon.getId(), coupon.getCreater());
                    }
                    // 替换dto.getIds中的优惠券ID为creator
                    List<Long> newIds = new ArrayList<>();
                    for (Long id : dto.getIds()) {
                        if (couponIdToCreatorMap.containsKey(id)) {
                            newIds.add(couponIdToCreatorMap.get(id));
                        } else {
                            // 如果找不到匹配的creator，保留原ID或做其他处理
                            newIds.add(id);
                        }
                    }
                    // 设置返回用户券id，不能返回原始券id！！！
                    dto.setIds(newIds);
                    return dto;
                }
            },discountSolutionExecutor).thenAccept(new Consumer<CouponDiscountDTO>() {
                @Override
                public void accept(CouponDiscountDTO dto) {
                    log.debug("方案最终优惠{}  方案中优惠券使用了{} 规则{}",dto.getDiscountAmount(),dto.getIds(),dto.getRules());
                    dtos.add(dto);
                    latch.countDown();
                }
            });
        }
        try{
            latch.await(2,TimeUnit.SECONDS);//主线程最多阻塞2秒
        }catch(Exception e){
            log.error("多线程优惠计算出错！",e);
        }

        //筛选最优解
        return findBestSolution(dtos);
    }

    //筛选最优解
    private List<CouponDiscountDTO> findBestSolution(List<CouponDiscountDTO> solutions) {
        //创建2个map 分别记录用券相同，金额最高     金额相同，用券最少
        Map<String,CouponDiscountDTO> moreDiscountMap=new HashMap<>();
        Map<Integer,CouponDiscountDTO> lessCouponMap = new HashMap<>();
        //循环方案 向map中记录  用券相同，金额最高     金额相同，用券最少
        for (CouponDiscountDTO solution : solutions) {
            //对优惠券id  升序，转字符串  然后以逗号拼接
            String ids = solution.getIds().stream().sorted(Comparator.comparing(Long::longValue)).map(String::valueOf).collect(Collectors.joining(","));
            //从moreDiscountMap中取 旧的记录 判断  如果当前方案的优惠金额 小于 旧的方案金额 当前方案忽略，处理下一个方案
            CouponDiscountDTO old = moreDiscountMap.get(ids);
            if(old!=null && old.getDiscountAmount()>=solution.getDiscountAmount()){
                continue;
            }
            //从lessCouponMap中取旧的记录 判断 如果当前方案用券数量 大于 旧的方案用券数量 前方案忽略，处理下一个方案
            old=lessCouponMap.get(solution.getDiscountAmount());
            int newSize = solution.getIds().size();
            if(old!=null && newSize>1 && old.getIds().size()<=newSize){
                continue;
            }

            //添加更优的方案到map中
            moreDiscountMap.put(ids,solution);
            lessCouponMap.put(solution.getDiscountAmount(),solution);
        }

        //求2个map的交集
        Collection<CouponDiscountDTO> bestSolution = CollUtils
                .intersection(moreDiscountMap.values(), lessCouponMap.values());
        // 按优惠金额降序
        return bestSolution.stream()
                .sorted(Comparator.comparingInt(CouponDiscountDTO::getDiscountAmount).reversed())
                .collect(Collectors.toList());


    }

    //计算每一个方案的优惠明细
    public CouponDiscountDTO calculateSolutionDiscount(Map<Coupon, List<OrderCourseDTO>> avaMap,
                                                        List<OrderCourseDTO> courses,
                                                        List<Coupon> solution) {
        //创建方案结果dto对象
        CouponDiscountDTO dto =new CouponDiscountDTO();
        //初始化商品id和商品折扣明细的映射，初始折扣明细全都设置为0
        Map<Long, Integer> detailMap = courses.stream().collect(Collectors.toMap(OrderCourseDTO::getId, orderCourseDTO -> 0));
        dto.setDiscountDetail(detailMap);
        //计算该方案的优惠信息
        //循环方案中的优惠券
        for (Coupon coupon : solution) {
            //取出该优惠券对应的可用课程
            List<OrderCourseDTO> availableCourses = avaMap.get(coupon);
            //计算可用课程的总金额（商品价格-该商品的折扣明细）
            int totalAmount = availableCourses.stream().mapToInt(value -> value.getPrice() - detailMap.get(value.getId())).sum();
            //判断优惠券是否可用
            Discount discount =DiscountStrategy.getDiscount(coupon.getDiscountType());
            if(!discount.canUse(totalAmount,coupon)){
                continue;//券不可用
            }
            //计算该优惠券使用后的折扣值
            int discountAmount = discount.calculateDiscount(totalAmount, coupon);
            //更新商品的折扣明细（更新商品id的商品折扣明细）并且更新到detailMap中
            calculateDetailDiscount(detailMap,availableCourses,totalAmount,discountAmount);
            //累加每一个优惠券的优惠金额。赋值给方案结果dto对象
            dto.getIds().add(coupon.getId());//只要执行这句话，证明该优惠券生效了
            dto.getRules().add(discount.getRule(coupon));
            dto.setDiscountAmount(discountAmount+dto.getDiscountAmount());//不能覆盖，应该是所有优惠券累加的结果
        }
        return dto;
    }



    /**
     * 计算商品折扣明细
     * @param detailMap 商品id和商品优惠明细的映射
     * @param availableCourses 当前优惠券可用的课程集合
     * @param totalAmount 可用课程的总金额
     * @param discountAmount 当前优惠券能优惠的金额
     */
    private void calculateDetailDiscount(Map<Long, Integer> detailMap, List<OrderCourseDTO> availableCourses, int totalAmount,int discountAmount) {
        //目的：本方法是在优惠券使用后，计算每个商品的折扣明细
        //规则：前面的商品按比例计算，最后一个商品的折扣明细=总的优惠金额-前面商品优惠的总额
        //循环可用商品
        int times = 0;//代表已处理的商品个数
        int remainDiscount = discountAmount;//代表剩余的优惠金额
        for (OrderCourseDTO c : availableCourses) {
            times++;
            int discount = 0 ;
            if(times==availableCourses.size()){
                //说明是最后一个课程
                discount = remainDiscount;
            }else{
                //是前面的课程，按比例
                discount = c.getPrice()*discountAmount/totalAmount;//此处先乘再除 否则结果是0
                remainDiscount=remainDiscount-discount;
            }
            //将商品的折扣明细 添加到detailMap 累加
            detailMap.put(c.getId(),discount+detailMap.get(c.getId()));
        }


    }


    //细筛，查询每个优惠券对应的可用课程
    @Override
    public  Map<Coupon, List<OrderCourseDTO>> findAvailableCoupons(List<Coupon> coupons, List<OrderCourseDTO> orderCourses) {
        Map<Coupon, List<OrderCourseDTO>> map =new HashMap<>();
        //循环遍历初筛后的优惠券集合
        for (Coupon coupon : coupons) {
            //找出每一个优惠券的可用课程
            List<OrderCourseDTO> availableCourses =orderCourses;
            //判断优惠券是否限定了范围 coupon.specific 为true
            if(coupon.getSpecific()){
                //查询限定范围 查询coupon_scope 表 条件为coupon_id
                List<CouponScope> scopeList = scopeService.lambdaQuery().eq(CouponScope::getCouponId, coupon.getId()).list();
                //得到限定范围的id集合
                List<Long> scopeIds = scopeList.stream().map(CouponScope::getBizId).collect(Collectors.toList());
                //从orderCourses订单中所有的课程集合 筛选 该范围内的课程
                availableCourses= orderCourses.stream().filter(orderCourseDTO -> scopeIds.contains(orderCourseDTO.getCateId()))
                        .collect(Collectors.toList());
            }
            if(CollUtils.isEmpty(availableCourses)){
                 continue;//说明当前优惠券限定了范围，但是在订单中的课程中没有找到可用课程，说明该券不可用，忽略该券，对下一个优惠券处理
            }
            //计算该优惠券 可用课程的总金额
            int totalAmount = availableCourses.stream().mapToInt(OrderCourseDTO::getPrice).sum();
            //判断该优惠券是否可用，若可用添加到map中
            Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
            if(discount.canUse(totalAmount,coupon)){
                map.put(coupon,availableCourses);
            }
        }
        return map;
    }

    //核销指定优惠券
    @Override
    @Transactional
    public void writeOffCoupon(List<Long> userCouponIds) {
        // 1.查询优惠券
        List<UserCoupon> userCoupons = listByIds(userCouponIds);
        if (CollUtils.isEmpty(userCoupons)) {
            return;
        }
        // 2.处理数据
        List<UserCoupon> list = userCoupons.stream()
                // 过滤无效券
                .filter(coupon -> {
                    if (coupon == null) {
                        return false;
                    }
                    if (UserCouponStatus.UNUSED != coupon.getStatus()) {
                        return false;
                    }
                    //注意校验优惠券过期时间
                    LocalDateTime now = LocalDateTime.now();
                    return !now.isBefore(coupon.getTermBeginTime()) && !now.isAfter(coupon.getTermEndTime());
                })
                // 组织新增数据
                .map(coupon -> {
                    UserCoupon c = new UserCoupon();
                    c.setId(coupon.getId());
                    c.setStatus(UserCouponStatus.USED);
                    c.setUsedTime(LocalDateTime.now());
                    return c;
                })
                .collect(Collectors.toList());

        // 4.核销，修改优惠券状态
        boolean success = updateBatchById(list);
        if (!success) {
            return;
        }
        // 5.更新已使用数量
        List<Long> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        for (Long couponId : couponIds) {
            int c = couponMapper.incrIssueNum(couponId);
            if (c < 1) {
                throw new DbException("更新优惠券使用数量失败！");
            }
        }

    }

    //退还指定优惠券
    @Override
    @Transactional
    public void refundCoupon(List<Long> userCouponIds) {
        // 1.查询优惠券
        List<UserCoupon> userCoupons = listByIds(userCouponIds);
        if (CollUtils.isEmpty(userCoupons)) {
            return;
        }
        // 2.处理优惠券数据
        List<UserCoupon> list = userCoupons.stream()
                // 过滤无效券
                .filter(coupon -> coupon != null && UserCouponStatus.USED == coupon.getStatus())
                // 更新状态字段
                .map(coupon -> {
                    UserCoupon c = new UserCoupon();
                    c.setId(coupon.getId());
                    // 3.判断有效期，是否已经过期，如果过期，则状态为 已过期，否则状态为 未使用
                    LocalDateTime now = LocalDateTime.now();
                    UserCouponStatus status = now.isAfter(coupon.getTermEndTime()) ?
                            UserCouponStatus.EXPIRED : UserCouponStatus.UNUSED;
                    c.setStatus(status);
                    return c;
                }).collect(Collectors.toList());

        // 4.修改优惠券状态
        boolean success = updateBatchById(list);
        if (!success) {
            return;
        }
        List<Long> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        for (Long couponId : couponIds) {
            int c = couponMapper.incrIssueNum(couponId);
            if (c < 1) {
                throw new DbException("更新优惠券使用数量失败！");
            }
        }
    }

    //分页查询我的优惠券接口
    @Override
    public List<String> queryDiscountRules(List<Long> userCouponIds) {
        // 1.查询优惠券信息
        List<Coupon> coupons = baseMapper.queryCouponByUserCouponIds(userCouponIds, UserCouponStatus.USED);
        if (CollUtils.isEmpty(coupons)) {
            return CollUtils.emptyList();
        }
        // 2.转换规则
        return coupons.stream()
                .map(c -> DiscountStrategy.getDiscount(c.getDiscountType()).getRule(c))
                .collect(Collectors.toList());
    }
    @Override
    public CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO) {
        // 1.查询用户优惠券
        List<Long> userCouponIds = orderCouponDTO.getUserCouponIds();
        List<Coupon> coupons = userCouponMapper.queryCouponByUserCouponIds(userCouponIds, UserCouponStatus.UNUSED);
        if (CollUtils.isEmpty(coupons)) {
            return null;
        }
        // 2.查询优惠券对应课程
        Map<Coupon, List<OrderCourseDTO>> availableCouponMap = findAvailableCoupons(coupons, orderCouponDTO.getCourseList());
        if (CollUtils.isEmpty(availableCouponMap)) {
            return null;
        }
        // 3.查询优惠券规则
        return calculateSolutionDiscount(availableCouponMap, orderCouponDTO.getCourseList(), coupons);
    }

    @Override
    public List<Long> transformCouponIds(List<Long> couponIds) {
        // 获取用户所有优惠券的ID集合（使用HashSet提高查找效率）
        Map<Long, Long> couponIdToCreatorMap = userCouponMapper.queryMyCoupons(UserContext.getUser())
                .stream()
                .collect(Collectors.toMap(
                        Coupon::getId,       // 键：优惠券ID
                        Coupon::getCreater,  // 值：creator
                        (existing, replacement) -> existing  // 处理重复键的策略（保留第一个）
                ));

        // 遍历输入ID列表，替换为对应的creator（O(m)时间）
        return couponIds.stream()
                .map(id -> couponIdToCreatorMap.getOrDefault(id, id))  // 存在则替换为creator，否则保留原ID
                .collect(Collectors.toList());
    }
}
