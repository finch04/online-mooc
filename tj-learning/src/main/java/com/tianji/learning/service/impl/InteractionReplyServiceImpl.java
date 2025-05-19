package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.remark.RemarkClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.LearningConstants;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.IInteractionReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.tianji.common.constants.Constant.DATA_FIELD_NAME_CREATE_TIME;
import static com.tianji.common.constants.Constant.DATA_FIELD_NAME_LIKED_TIME;

/**
 * <p>
 * 互动问题的回答或评论 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-23
 */
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply> implements IInteractionReplyService {

    private final UserClient userClient;
    private final InteractionQuestionMapper questionMapper;
    private final RemarkClient remarkClient;
    private final RabbitMqHelper mqHelper;

    //新增评论
    @Override
    public void saveReply(ReplyDTO dto) {
        Long userId = UserContext.getUser();

        InteractionReply reply = BeanUtils.copyBean(dto, InteractionReply.class);
        reply.setUserId(userId);
        this.save(reply);

        InteractionQuestion question=questionMapper.selectById(dto.getQuestionId());

        if(dto.getAnswerId()!=null){
            //这是评论 累加回答的评论次数
            InteractionReply target = this.getById(dto.getAnswerId());
            if(target==null){
                throw new BadRequestException("评论不存在！");
            }
            target.setReplyTimes(target.getReplyTimes()+1);
            this.updateById(target);
        }else{
            //这是回答 修改问题表最近一次回答id 同时累加问题表回答次数
            question.setAnswerTimes(question.getAnswerTimes()+1);
            question.setLatestAnswerId(reply.getId());

        }
        //判断是否为学生提交
        if(dto.getIsStudent()){
            question.setStatus(QuestionStatus.UN_CHECK);
            // 学生才需要累加积分
            mqHelper.send(MqConstants.Exchange.LEARNING_EXCHANGE,
                    MqConstants.Key.WRITE_REPLY,
                    SignInMessage.of(userId,LearningConstants.REWARD_WRITE_REPLY)
            );
        }

        questionMapper.updateById(question);
    }

    //分页查询评论列表 用户端/管理端
    @Override
    public PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery query,boolean forAdmin) {
//        if(query.getQuestionId()==null && query.getAnswerId()==null){
//            throw new BadRequestException("问题或回答不存在！");
//        }
//        Page<InteractionReply> page =this.lambdaQuery()
//                    .eq(query.getQuestionId()!=null,InteractionReply::getQuestionId,query.getQuestionId())
//                    .eq(InteractionReply::getAnswerId, query.getAnswerId()==null?0L:query.getAnswerId())
//                    .eq(InteractionReply::getHidden, false)
//                    .page(query.toMpPage(//先按照点赞数排序，点赞数相同则按照创建时间排序
//                            new OrderItem(DATA_FIELD_NAME_LIKED_TIME,false),
//                            new OrderItem(DATA_FIELD_NAME_CREATE_TIME,false)
//                             ));
//        List<InteractionReply> records = page.getRecords();
//        if(CollUtils.isEmpty(records)){
//            return PageDTO.empty(0L,0L);
//        }
//        Set<Long> uids=new HashSet<>(); //评论的用户id集合
//        Set<Long> targetReplyIds=new HashSet<>(); //评论的目标回答id集合
//        for (InteractionReply record : records) {
//            if(!record.getAnonymity()){//如果用户是匿名提问，则不显示用户名和头像
//                uids.add(record.getUserId());
//                uids.add(record.getTargetUserId());
//            }
//            if(record.getTargetReplyId()!=null && record.getTargetReplyId()>0){
//                targetReplyIds.add(record.getTargetReplyId());
//            }
//        }
//        //查询目标回复，如果目标回复不是匿名，则需要查询目标回复的用户信息
//        if(targetReplyIds.size()>0){
//            List<InteractionReply> targetReplies = listByIds(targetReplyIds);
//            Set<Long> targetUserIds = targetReplies.stream()
//                    .filter(Predicate.not(InteractionReply::getAnonymity))
//                    .map(InteractionReply::getUserId)
//                    .collect(Collectors.toSet());
//            uids.addAll(targetUserIds);
//        }
//        //远程调用用户服务 获取用户信息
//        List<UserDTO> userDTOS = userClient.queryUserByIds(uids);
//        Map<Long, UserDTO> userDTOMap=new HashMap<>();
//        if(userDTOS!=null) {
//            userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));
//        }
//        //封装vo
//        List<ReplyVO> voList=new ArrayList<>();
//        for (InteractionReply record : records) {
//            ReplyVO vo =BeanUtils.copyBean(record,ReplyVO.class);
//            if(!record.getAnonymity()){
//                UserDTO userDTO = userDTOMap.get(record.getUserId());
//                if(userDTO!=null){
//                    vo.setUserName(userDTO.getName());
//                    vo.setUserIcon(userDTO.getIcon());
//                    vo.setUserType(userDTO.getType());
//                }
//                UserDTO targetUserDTO = userDTOMap.get(record.getTargetUserId());
//                if(targetUserDTO!=null){
//                    vo.setTargetUserName(targetUserDTO.getName());
//                }
//            }
//            voList.add(vo);
//        }
//        return PageDTO.of(page,voList);
        // 1.问题id和回答id至少要有一个，先做参数判断
        Long questionId = query.getQuestionId();
        Long answerId = query.getAnswerId();
        if (questionId == null && answerId == null) {
            throw new BadRequestException("问题或回答id不能都为空");
        }
        // 标记当前是查询问题下的回答
        boolean isQueryAnswer = questionId != null;
        // 2.分页查询reply
        Page<InteractionReply> page = lambdaQuery()
                .eq(isQueryAnswer, InteractionReply::getQuestionId, questionId)
                .eq(InteractionReply::getAnswerId, isQueryAnswer ? 0L : answerId)
                .eq(!forAdmin, InteractionReply::getHidden, false)
                .page(query.toMpPage( // 先根据点赞数排序，点赞数相同，再按照创建时间排序
                        new OrderItem(DATA_FIELD_NAME_LIKED_TIME, false),
                        new OrderItem(DATA_FIELD_NAME_CREATE_TIME, true))
                );
        List<InteractionReply> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.数据处理，需要查询：提问者信息、回复目标信息、当前用户是否点赞
        Set<Long> userIds = new HashSet<>();
        Set<Long> answerIds = new HashSet<>();
        Set<Long> targetReplyIds = new HashSet<>();
        // 3.1.获取提问者id 、回复的目标id、当前回答或评论id（统计点赞信息）
        for (InteractionReply r : records) {
            if(!r.getAnonymity() || forAdmin) {
                // 非匿名
                userIds.add(r.getUserId());
            }
            targetReplyIds.add(r.getTargetReplyId());
            answerIds.add(r.getId());
        }
        // 3.2.查询目标回复，如果目标回复不是匿名，则需要查询出目标回复的用户信息
        targetReplyIds.remove(0L);
        targetReplyIds.remove(null);
        if(targetReplyIds.size() > 0) {
            List<InteractionReply> targetReplies = listByIds(targetReplyIds);
            Set<Long> targetUserIds = targetReplies.stream()
                    .filter(Predicate.not(InteractionReply::getAnonymity).or(r -> forAdmin))
                    .map(InteractionReply::getUserId)
                    .collect(Collectors.toSet());
            userIds.addAll(targetUserIds);
        }
        // 3.3.查询用户
        Map<Long, UserDTO> userMap = new HashMap<>(userIds.size());
        if(userIds.size() > 0) {
            List<UserDTO> users = userClient.queryUserByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        }
        // 3.4.查询用户点赞状态
        Set<Long> bizLiked = remarkClient.getLikesStatusByBizIds(answerIds);
        // 4.处理VO
        List<ReplyVO> list = new ArrayList<>(records.size());
        for (InteractionReply r : records) {
            // 4.1.拷贝基础属性
            ReplyVO v = BeanUtils.toBean(r, ReplyVO.class);
            list.add(v);
            // 4.2.回复人信息
            if(!r.getAnonymity() || forAdmin){
                UserDTO userDTO = userMap.get(r.getUserId());
                if (userDTO != null) {
                    v.setUserIcon(userDTO.getIcon());
                    v.setUserName(userDTO.getName());
                    v.setUserType(userDTO.getType());
                }
            }
            // 4.3.如果存在评论的目标，则需要设置目标用户信息
            if(r.getTargetReplyId() != null){
                UserDTO targetUser = userMap.get(r.getTargetUserId());
                if (targetUser != null) {
                    v.setTargetUserName(targetUser.getName());
                }
            }
            // 4.4.点赞状态
            v.setLiked(bizLiked.contains(r.getId()));
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }

    //管理端隐藏评论
    @Override
    public void hiddenReply(Long id, Boolean hidden) {
        InteractionReply reply = this.getById(id);
        if(reply==null){
            throw new BadRequestException("回答不存在！");
        }
        reply.setHidden(hidden);
        updateById(reply);
        // 隐藏评论，先判断是否是回答，回答才需要隐藏下属评论
        if (reply.getAnswerId() != null && reply.getAnswerId() != 0) {
            // 有answerId，说明自己是评论，无需处理
            return;
        }
        // 没有answerId，说明自己是回答，需要隐藏回答下的评论
        lambdaUpdate()
                .set(InteractionReply::getHidden, hidden)
                .eq(InteractionReply::getAnswerId, id)
                .update();

    }


    //管理端按照id查询评论
    @Override
    public ReplyVO queryReplyById(Long id) {
        // 1.根据id查询
        InteractionReply r = getById(id);
        // 2.数据处理，需要查询用户信息、评论目标信息、当前用户是否点赞
        Set<Long> userIds = new HashSet<>();
        // 2.1.获取用户 id
        userIds.add(r.getUserId());
        // 2.2.查询评论目标，如果评论目标不是匿名，则需要查询出目标回复的用户id
        if(r.getTargetReplyId() != null && r.getTargetReplyId() != 0) {
            InteractionReply target = getById(r.getTargetReplyId());
            if(!target.getAnonymity()) {
                userIds.add(target.getUserId());
            }
        }
        // 2.3.查询用户详细
        Map<Long, UserDTO> userMap = new HashMap<>(userIds.size());
        if(userIds.size() > 0) {
            List<UserDTO> users = userClient.queryUserByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        }
        // 2.4.查询用户点赞状态
       Set<Long> bizLiked = remarkClient.getLikesStatusByBizIds(CollUtils.singletonList(id));
        // 4.处理VO
        // 4.1.拷贝基础属性
        ReplyVO v = BeanUtils.toBean(r, ReplyVO.class);
        // 4.2.回复人信息
        UserDTO userDTO = userMap.get(r.getUserId());
        if (userDTO != null) {
            v.setUserIcon(userDTO.getIcon());
            v.setUserName(userDTO.getName());
            v.setUserType(userDTO.getType());
        }
        // 4.3.目标用户
        UserDTO targetUser = userMap.get(r.getTargetUserId());
        if (targetUser != null) {
            v.setTargetUserName(targetUser.getName());
        }
        //4.4.点赞状态
        v.setLiked(bizLiked.contains(id));
        return v;

    }
}
