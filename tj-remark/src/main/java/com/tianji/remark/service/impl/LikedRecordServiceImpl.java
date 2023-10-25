//package com.tianji.remark.service.impl;
//
//import com.tianji.api.dto.msg.LikedTimesDTO;
//import com.tianji.api.dto.trade.OrderBasicDTO;
//import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
//import com.tianji.common.constants.MqConstants;
//import com.tianji.common.utils.StringUtils;
//import com.tianji.common.utils.UserContext;
//import com.tianji.remark.domain.dto.LikeRecordFormDTO;
//
//import com.tianji.remark.domain.po.LikedRecord;
//import com.tianji.remark.mapper.LikedRecordMapper;
//import com.tianji.remark.service.ILikedRecordService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.net.UnknownServiceException;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * <p>
// * 点赞记录表 服务实现类
// * </p>
// *
// * @author fsq
// * @since 2023-10-25
// */
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService {
//
//    private final RabbitMqHelper rabbitMqHelper;
//
//    //点赞或取消赞
//    @Override
//    public void addLikeRecord(LikeRecordFormDTO dto) {
//        //获取当前用户
//        Long userId = UserContext.getUser();
//        //判断是否点赞 dto.liked true为点赞
//        boolean flag= dto.getLiked()?liked(dto,userId):unliked(dto,userId);
//        if(!flag){//说明点赞或取消赞失败
//            return;
//        }
//        //统计该业务id的总点赞数
//        Integer totalLikesNum = this.lambdaQuery().eq(LikedRecord::getBizId, dto.getBizId()).count();
//        //发送消息到mq
//        String routingKey = StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE,dto.getBizType());
//
//
////        LikedTimesDTO msg =new LikedTimesDTO();
////        msg.setLikedTimes(totalLikesNum);
////        msg.setBizId(dto.getBizId());
//
////        LikedTimesDTO msg = LikedTimesDTO.builder().likedTimes(totalLikesNum).bizId(dto.getBizId()).build();
//
//        LikedTimesDTO msg = LikedTimesDTO.of( dto.getBizId(),totalLikesNum);
//        log.info("发送点赞消息 ：{}",msg);
//        rabbitMqHelper.send(
//                MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
//                routingKey,
//                msg
//        );
//
//    }
//
//    //取消点赞
//    private boolean unliked(LikeRecordFormDTO dto,Long userId) {
//        LikedRecord record = this.lambdaQuery()
//                .eq(LikedRecord::getUserId, userId)
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .one();
//        if(record==null){
//            //说明之前没点过赞
//            return false;
//        }
//
//        return removeById(record.getId());
//    }
//
//    //点赞
//    private boolean liked(LikeRecordFormDTO dto,Long userId) {
//        LikedRecord record = this.lambdaQuery()
//                .eq(LikedRecord::getUserId, userId)
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .one();
//        if(record!=null){
//            //说明之前点过赞
//            return false;
//        }
//
//        LikedRecord likedRecord =new LikedRecord();
//        likedRecord.setUserId(userId);
//        likedRecord.setBizId(dto.getBizId());
//        likedRecord.setBizType(dto.getBizType());
//        return this.save(likedRecord);
//
//    }
//
//    //查询点赞状态
//    @Override
//    public Set<Long> getLikesStatusByBizIds(List<Long> bizIds) {
//        //获取用户id
//        Long userId =UserContext.getUser();
//        //查点赞记录表 in bizIds
//        List<LikedRecord> recordList = this.lambdaQuery().in(LikedRecord::getBizId, bizIds).eq(LikedRecord::getUserId, userId).list();
//        //将查询到的bizId 转集合返回
//        Set<Long> likedBizIds = recordList.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());
//
//        return likedBizIds;
//    }
//
//}
