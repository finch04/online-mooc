package com.tianji.learning.service.impl;/**
 * @author fsq
 * @date 2025/5/22 11:39
 */

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.remark.RemarkClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.leanring.EvaluationScoreDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.*;
import com.tianji.learning.constants.LearningConstants;
import com.tianji.learning.domain.dto.EvaluationDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.*;
import com.tianji.learning.domain.query.EvaluationQuery;
import com.tianji.learning.domain.vo.EvaluationDetailVO;
import com.tianji.learning.domain.vo.EvaluationVO;
import com.tianji.learning.domain.vo.LessonCollectVO;
import com.tianji.learning.domain.vo.NoteVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.mapper.EvaluationMapper;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.IEvaluationService;
import com.tianji.learning.service.IInteractionQuestionService;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.common.constants.MqConstants.Key.COURSE_COMMENT;
import static com.tianji.common.constants.MqConstants.Key.NOTE_GATHERED;

/**
 * @Author: fsq
 * @Date: 2025/5/22 11:39
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements IEvaluationService {

    private final UserClient userClient;
    private final CourseClient courseClient;
    private final RemarkClient remarkClient;
    private final RabbitMqHelper mqHelper;
    private final ILearningLessonService  lessonService;

    @Override
    public PageDTO<EvaluationVO> queryEvaluationPage(EvaluationQuery query) {
        Long userId = UserContext.getUser();
        //分页查询我的课表
        Page<Evaluation> page = query.getOnlyMine() ?
                this.lambdaQuery()
                        .eq(Evaluation::getCourseId, query.getCourseId())
                        .eq(Evaluation::getUserId, userId).eq(Evaluation::getHidden, false)
                        .eq(query.getTeacherId() != null, Evaluation::getTeacherId, query.getTeacherId())
                        .page(query.toMpPage("create_time", false))
                :
                this.lambdaQuery()
                        .eq(Evaluation::getCourseId, query.getCourseId())
                        .eq(query.getTeacherId() != null, Evaluation::getTeacherId, query.getTeacherId())
                        .eq(Evaluation::getHidden, false)
                        .page(query.toMpPage("create_time", false));

        List<Evaluation> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        // 收集非匿名用户ID和教师ID
        Set<Long> userIds = records.stream()
                .filter(record -> !Boolean.TRUE.equals(record.getAnonymity()) && record.getUserId() != null)
                .map(Evaluation::getUserId)
                .collect(Collectors.toSet());

        Set<Long> teacherIds = records.stream()
                .filter(record -> record.getTeacherId() != null)
                .map(Evaluation::getTeacherId)
                .collect(Collectors.toSet());

        // 合并ID集合，一次性查询
        Set<Long> allIds = new HashSet<>(userIds);
        allIds.addAll(teacherIds);

        Map<Long, UserDTO> userDTOMap;
        if (!allIds.isEmpty()) {
            List<UserDTO> userDTOS = userClient.queryUserByIds(allIds);
            userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        } else {
            userDTOMap = Collections.emptyMap();
        }

        // 查询用户点赞状态
        Set<Long> bizLiked = remarkClient.getLikesStatusByBizIds(
                records.stream().map(Evaluation::getId).collect(Collectors.toSet()));

        // 转换为VO，确保匿名评价不显示用户信息
        List<EvaluationVO> voList = records.stream().map(record -> {
            EvaluationVO vo = BeanUtils.copyBean(record, EvaluationVO.class);

            // 设置用户信息（非匿名且userId不为空）
            if (!Boolean.TRUE.equals(record.getAnonymity()) && record.getUserId() != null) {
                UserDTO userDTO = userDTOMap.get(record.getUserId());
                if (userDTO != null) {
                    vo.setUserName(userDTO.getName());
                    vo.setUserIcon(userDTO.getIcon());
                }
            }

            // 设置教师信息（teacherId不为空）
            if (record.getTeacherId() != null) {
                UserDTO userDTO = userDTOMap.get(record.getTeacherId());
                if (userDTO != null) {
                    vo.setTeacherName(userDTO.getName());
                }
            }

            vo.setIsHelpful(bizLiked.contains(record.getId()));
            vo.setOverallRating(calculateOverallRating(record));
            return vo;
        }).collect(Collectors.toList());

        return PageDTO.of(page, voList);
    }

    //计算综合评分
    private Double calculateOverallRating(Evaluation evaluation){
        int contentRating = evaluation.getContentRating();
        int teachingRating = evaluation.getTeachingRating();
        int difficultyRating = evaluation.getDifficultyRating();
        int valueRating = evaluation.getValueRating();
        return (contentRating + teachingRating + difficultyRating + valueRating) / 4.0;
    }

    @Override
    public void saveEvaluation(EvaluationDTO dto) {
        Long userId = UserContext.getUser();
        //购买过才可以评价课程
        LearningLesson lesson = lessonService.lambdaQuery()
                .eq(LearningLesson::getCourseId, dto.getCourseId())
                .eq(LearningLesson::getUserId, userId).one();
        if(lesson==null){
            throw new BizIllegalException("只能评价购买过的课程");
        }

        Evaluation evaluation = BeanUtils.copyBean(dto, Evaluation.class);
        evaluation.setUserId(userId);

        this.save(evaluation);
        // 发送mq消息 增加积分
        mqHelper.send(MqConstants.Exchange.LEARNING_EXCHANGE,
                COURSE_COMMENT,
                SignInMessage.of(userId, LearningConstants.REWARD_COURSE_COMMENT)
        );
    }

    @Override
    public Boolean updateEvaluation(EvaluationDTO dto) {
        Long userId = UserContext.getUser();
        Evaluation evaluation = getById(dto.getId());
        AssertUtils.isNotNull(evaluation, "非法的id");
        AssertUtils.isTrue(evaluation.getUserId().equals(userId), "无权限修改");

        evaluation = BeanUtils.copyBean(dto, Evaluation.class);
        return updateById(evaluation);
    }


    @Override
    public EvaluationDetailVO queryEvaluationDetailById(Long id) {
        Long userId = UserContext.getUser();

        Evaluation evaluation = getById(id);
        if(evaluation==null){
            throw new BizIllegalException("非法的id");
        }
        EvaluationDetailVO vo = new EvaluationDetailVO();
        BeanUtils.copyProperties(evaluation,vo);
        vo.setOverallRating(calculateOverallRating(evaluation));

        if(!evaluation.getAnonymity()){
            UserDTO dto = userClient.queryUserById(evaluation.getUserId());
            if(dto!=null){
                vo.setUserName(dto.getName());
                vo.setUserIcon(dto.getIcon());
            }
        }
        // 查询用户点赞状态
        Set<Long> bizLiked = remarkClient.getLikesStatusByBizIds(Collections.singletonList(id));
        if(bizLiked.contains(userId)){
            vo.setIsHelpful(true);
        }

        return vo;
    }

    @Override
    public EvaluationDTO queryEvaluationById(Long id) {
        Evaluation evaluation = this.getById(id);
        return BeanUtils.copyBean(evaluation, EvaluationDTO.class);
    }

    @Override
    public Boolean deleteEvaluation(Long id) {
        Evaluation evaluation = getById(id);
        if(evaluation==null){
            throw new BizIllegalException("非法的id");
        }
        Long userId = UserContext.getUser();
        AssertUtils.isTrue(evaluation.getUserId().equals(userId), "无权限删除");
        return removeById(id);
    }

    @Override
    public Boolean isEvaluated(Long courseId) {
        Long userId = UserContext.getUser();
        //购买过才可以评价课程
        LearningLesson lesson = lessonService.lambdaQuery()
                .eq(LearningLesson::getCourseId, courseId)
                .eq(LearningLesson::getUserId, userId).one();
        if(lesson==null){
            return true;
        }

        Integer count = this.lambdaQuery()
                .eq(Evaluation::getCourseId, courseId)
                .eq(Evaluation::getUserId, userId)
                .eq(Evaluation::getHidden, false).count();
        return count > 0;
    }

    @Override
    public void getAllCourseAvgScore() {
        // 根据课程id 计算各个维度平均分
        List<Evaluation> list = this.lambdaQuery()
                .eq(Evaluation::getHidden, false)
                .list();

        // 按课程ID分组并计算平均分
        Map<Long, List<Evaluation>> courseEvaluationMap = list.stream()
                .collect(Collectors.groupingBy(Evaluation::getCourseId));

        List<EvaluationScoreDTO> dtoList = courseEvaluationMap.entrySet().stream()
                .map(entry -> {
                    Long courseId = entry.getKey();
                    List<Evaluation> courseEvaluations = entry.getValue();
                    int commentCount = courseEvaluations.size();

                    // 如果没有评论，避免除零错误
                    if (commentCount == 0) {
                        EvaluationScoreDTO dto = new EvaluationScoreDTO();
                        dto.setCourseId(courseId);
                        dto.setCommentCount(0);
                        return dto;
                    }

                    // 计算每个维度的总分
                    int contentTotal = 0;
                    int teachingTotal = 0;
                    int difficultyTotal = 0;
                    int valueTotal = 0;
                    double overallTotal = 0D;

                    for (Evaluation evaluation : courseEvaluations) {
                        contentTotal += evaluation.getContentRating();
                        teachingTotal += evaluation.getTeachingRating();
                        difficultyTotal += evaluation.getDifficultyRating();
                        valueTotal += evaluation.getValueRating();
                        overallTotal += calculateOverallRating(evaluation);
                    }

                    // 计算平均分
                    EvaluationScoreDTO dto = new EvaluationScoreDTO();
                    dto.setCourseId(courseId);
                    dto.setCommentCount(commentCount);
                    dto.setContentRating(contentTotal / commentCount);
                    dto.setTeachingRating(teachingTotal / commentCount);
                    dto.setDifficultyRating(difficultyTotal / commentCount);
                    dto.setValueRating(valueTotal / commentCount);
                    dto.setOverallRating(overallTotal / commentCount);

                    return dto;
                })
                .collect(Collectors.toList());

        // 发送MQ消息到课程微服务
        mqHelper.send(MqConstants.Exchange.COURSE_EXCHANGE,
                MqConstants.Key.COURSE_COMMENT_KEY,
                dtoList);
    }
}
