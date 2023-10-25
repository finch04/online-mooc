package com.tianji.remark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.dto.msg.LikedTimesDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.remark.constants.RedisConstants;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LikedRecordRedisServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService {

    private final RabbitMqHelper rabbitMqHelper;
    private final StringRedisTemplate redisTemplate;

    //点赞或取消赞
    @Override
    public void addLikeRecord(LikeRecordFormDTO dto) {
        //获取当前用户
        Long userId = UserContext.getUser();
        //判断是否点赞 dto.liked true为点赞
        boolean flag= dto.getLiked()?liked(dto,userId):unliked(dto,userId);
        if(!flag){//说明点赞或取消赞失败
            return;
        }
        //统计该业务id的总点赞数
//        Integer totalLikesNum = this.lambdaQuery().eq(LikedRecord::getBizId, dto.getBizId()).count();

        //基于redis统计 业务id的总点赞量
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX+dto.getBizId();
        Long totalLikesNums = redisTemplate.opsForSet().size(key);
        if(totalLikesNums==null){
            return ;
        }
        //采用zset结构缓存点赞的总数
        String bizTypeTotalLikeKey=RedisConstants.LIKE_COUNT_KEY_PREFIX+dto.getBizType();
        redisTemplate.opsForZSet().add(bizTypeTotalLikeKey,dto.getBizId().toString(),totalLikesNums);


        /**
        //发送消息到mq
        String routingKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE,dto.getBizType());


        LikedTimesDTO msg =new LikedTimesDTO();
        msg.setLikedTimes(totalLikesNum);
        msg.setBizId(dto.getBizId());

        LikedTimesDTO msg = LikedTimesDTO.builder().likedTimes(totalLikesNum).bizId(dto.getBizId()).build();

        LikedTimesDTO msg = LikedTimesDTO.of( dto.getBizId(),totalLikesNum);
        log.info("发送点赞消息 ：{}",msg);
        rabbitMqHelper.send(
                MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                routingKey,
                msg
    );
        **/

    }

    //取消点赞
    private boolean unliked(LikeRecordFormDTO dto,Long userId) {
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX+dto.getBizId();
        Long result = redisTemplate.opsForSet().remove(key, userId.toString());
        return result!=null&&result>0;

        /**LikedRecord record = this.lambdaQuery()
                .eq(LikedRecord::getUserId, userId)
                .eq(LikedRecord::getBizId, dto.getBizId())
                .one();
        if(record==null){
            //说明之前没点过赞
            return false;
        }

     return removeById(record.getId());
         **/
    }

    //点赞
    private boolean liked(LikeRecordFormDTO dto,Long userId) {
        //基于redis做点赞
        //拼接key
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX+dto.getBizId();
//        redisTemplate.boundSetOps(key).add(userId.toString());
        Long result = redisTemplate.opsForSet().add(key, userId.toString());
        return result!=null&&result>0;

        /**LikedRecord record = this.lambdaQuery()
         .eq(LikedRecord::getUserId, userId)
         .eq(LikedRecord::getBizId, dto.getBizId())
         .one();
         if(record!=null){
         //说明之前点过赞
         return false;
         }

         LikedRecord likedRecord =new LikedRecord();
         likedRecord.setUserId(userId);
         likedRecord.setBizId(dto.getBizId());
         likedRecord.setBizType(dto.getBizType());
         return this.save(likedRecord);
         **/
    }

    //查询点赞状态
    @Override
    public Set<Long> getLikesStatusByBizIds(List<Long> bizIds) {
//        //获取用户
//        Long userId =UserContext.getUser();
//        if(CollUtils.isEmpty(bizIds)){
//            return CollUtils.emptySet();
//        }
//        //循环bizIds
//        Set<Long> likedBizIds =new HashSet<>();
//        for (Long bizId : bizIds) {
//           //判断该业务id 的点赞用户集合中是否包含当前用户
//            Boolean member = redisTemplate.opsForSet().isMember(RedisConstants.LIKE_BIZ_KEY_PREFIX + bizId, userId.toString());
//            if(member){
//                //判断当前业务，当前用户是否点赞
//                likedBizIds.add(bizId);
//            }
//        }
//        //如果有当前用户id则存入新集合返回
//        return likedBizIds;

        //------------------------用redis的管道方法去执行-----------------------
        // 1.获取登录用户id
        Long userId = UserContext.getUser();
        // 2.查询点赞状态
        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection src = (StringRedisConnection) connection;
            for (Long bizId : bizIds) {
                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + bizId;
                src.sIsMember(key, userId.toString());
            }
            return null;
        });
        // 3.返回结果
        return IntStream.range(0, objects.size()) // 创建从0到集合size的流
                .filter(i -> (boolean) objects.get(i)) // 遍历每个元素，保留结果为true的角标i
                .mapToObj(bizIds::get)// 用角标i取bizIds中的对应数据，就是点赞过的id
                .collect(Collectors.toSet());// 收集

    }

    //定时读取点赞数将其发送到mq
    @Override
    public void readLikedTimesAndSendMessage(String bizType, int maxBizSize) {
        //拼接key
        String bizTypeTotalLikeKey=RedisConstants.LIKE_COUNT_KEY_PREFIX+bizType;
        List<LikedTimesDTO> list =new ArrayList<>();
        //从zset结构中取maxBizSize数量的业务点赞信息  popmin获取并移除

        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().popMin(bizTypeTotalLikeKey, maxBizSize);
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            String bizId=typedTuple.getValue();
            Double likedTimes=typedTuple.getScore();
            if(StringUtils.isBlank(bizId)||likedTimes==null){
                continue;
            }
            //封装likedTimesDTO 消息数据
            LikedTimesDTO msg = LikedTimesDTO.of(Long.valueOf(bizId), likedTimes.intValue());
            list.add(msg);

        }
        //发送消息到mq
        if(CollUtils.isNotEmpty(list)){
            log.info("批量发送点赞消息 ：{}",list);
            String routingKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE,bizType);
            rabbitMqHelper.send(
                    MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                    routingKey,
                    list
            );
        }




    }
}
