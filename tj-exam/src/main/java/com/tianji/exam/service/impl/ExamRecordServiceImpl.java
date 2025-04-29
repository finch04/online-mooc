package com.tianji.exam.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.learning.LearningClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.exam.QuestionDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.*;
import com.tianji.exam.domain.dto.ExamCommitDTO;
import com.tianji.exam.domain.dto.ExamDetailDTO;
import com.tianji.exam.domain.dto.ExamFormDTO;
import com.tianji.exam.domain.po.ExamRecord;
import com.tianji.exam.domain.po.ExamRecordDetail;
import com.tianji.exam.domain.vo.ExamQuestionVO;
import com.tianji.exam.domain.vo.ExamRecordDetailVO;
import com.tianji.exam.domain.vo.ExamRecordVO;
import com.tianji.exam.enums.ExamType;
import com.tianji.exam.mapper.ExamRecordMapper;
import com.tianji.exam.service.IExamRecordDetailService;
import com.tianji.exam.service.IExamRecordService;
import com.tianji.exam.service.IQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试记录表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements IExamRecordService {

    private final LearningClient learningClient;

    private final IQuestionService questionService;

    private final IExamRecordDetailService recordDetailService;

    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;

    @Override
    public PageDTO<ExamRecordVO> queryMyExamRecordsPage(PageQuery query) {
        // 1.获取用户
        Long userId = UserContext.getUser();
        // 2.查询 练习没有分的过滤掉
        Page<ExamRecord> page = lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getFinished, true)
                .page(query.toMpPage("finish_time", false));
        List<ExamRecord> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.获取课程和章节信息
        Set<Long> courseIds = new HashSet<>();
        Set<Long> secIds = new HashSet<>();
        // 3.1.获取课程和小节id
        for (ExamRecord r : records) {
            courseIds.add(r.getCourseId());
            secIds.add(r.getSectionId());
        }
        // 3.2.查询课程
        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        Map<Long, String> courseMap = new HashMap<>(0);
        if(CollUtils.isNotEmpty(courseInfos)) {
            courseMap = courseInfos.stream()
                    .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, CourseSimpleInfoDTO::getName));
        }
        // 3.3.查询小节
        List<CataSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(secIds);
        Map<Long, String> cataMap = new HashMap<>(0);
        if(CollUtils.isNotEmpty(catas)) {
           cataMap = catas.stream()
                    .collect(Collectors.toMap(CataSimpleInfoDTO::getId, CataSimpleInfoDTO::getName));
        }
        // 4.结果处理
        List<ExamRecordVO> list = new ArrayList<>(records.size());
        for (ExamRecord r : records) {
            ExamRecordVO v = BeanUtils.toBean(r, ExamRecordVO.class);
            v.setCourseName(courseMap.getOrDefault(r.getCourseId(), "未知"));
            v.setSectionName(cataMap.getOrDefault(r.getSectionId(), "未知"));
            list.add(v);
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }

    @Override
    @Transactional
    public ExamQuestionVO saveExamRecord(ExamFormDTO examFormDTO) {
        // 1.判断当前用户是否购买了课程
        Long lessonId = learningClient.isLessonValid(examFormDTO.getCourseId());
        if (lessonId == null) {
            // 未购买的课程
            throw new BizIllegalException("请先购买课程!");
        }
        // 2.获取用户
        Long userId = UserContext.getUser();
        // 3.查询考试记录
        ExamRecord r = lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getSectionId, examFormDTO.getSectionId())
                .one();
        // 4.判断记录是否已经存在
        if (r != null) {
            // 4.1.已存在，则判断是否考试还是练习
            if (examFormDTO.getType() == ExamType.TEST) {
                // 4.2.是练习，清空旧数据
                removeById(r.getId());
                recordDetailService.removeByExamId(r.getId());
                r = null;
            }else if(r.getFinished()){
                // 4.3.是考试且已经提交，抛出异常
                throw new BadRequestException("不能重复考试！");
            }
        }

        if(r == null){
            // 5.新增考试记录
            r = new ExamRecord();
            r.setUserId(userId);
            r.setCourseId(examFormDTO.getCourseId());
            r.setSectionId(examFormDTO.getSectionId());
            r.setType(examFormDTO.getType());
            save(r);
        }

        // 6.查询考试题目
        List<QuestionDTO> questions = questionService.queryQuestionByBizId(examFormDTO.getSectionId());
        // 7.封装VO返回
        ExamQuestionVO vo = new ExamQuestionVO();
        vo.setQuestions(questions);
        vo.setId(r.getId());
        return vo;
    }

    @Override
    @Transactional
    public void saveExamRecordDetails(ExamCommitDTO examCommitDTO) {
        // 1.查询考试记录是否存在
        Long recordId = examCommitDTO.getId();
        Long userId = UserContext.getUser();
        log.info("用户{}提交考试记录{}", userId, examCommitDTO);
        ExamRecord record = getById(recordId);
        if (record == null) {
            throw new BadRequestException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            log.error("用户{}访问错误的考试记录{}", userId, recordId);
            throw new BadRequestException("错误的考试记录");
        }
        // 2.自动批阅答案并保存答案信息
        // 2.1.获取答案信息
        List<ExamDetailDTO> examDetails = examCommitDTO.getExamDetails();
        // 2.2.获取问题id
        List<Long> qIds = examDetails.stream()
                .filter(d -> StrUtil.isNotBlank(d.getAnswer()))
                .map(ExamDetailDTO::getQuestionId)
                .collect(Collectors.toList());
        // 2.3.查询问题
        Map<Long, QuestionDTO> qMap = new HashMap<>(qIds.size());
        if(CollUtils.isNotEmpty(qIds)) {
            List<QuestionDTO> qs = questionService.queryQuestionByIds(qIds);
            qMap = qs.stream().collect(Collectors.toMap(QuestionDTO::getId, s -> s));
        }
        // 2.4.组织考试明细数据
        int score = 0;
        int correct = 0;
        List<ExamRecordDetail> list = new ArrayList<>(examDetails.size());
        for (ExamDetailDTO dto : examDetails) {
            // 5.1.初始化
            ExamRecordDetail d = new ExamRecordDetail();
            d.setQuestionId(dto.getQuestionId());
            d.setAnswer(dto.getAnswer());
            d.setExamId(recordId);
            // 5.2.批阅客观题
            // 获取问题
            QuestionDTO question = qMap.get(d.getQuestionId());
            // 校验是否正确
            if (question != null && StringUtils.equals(question.getAnswer(), d.getAnswer())) {
                d.setCorrect(true);
                d.setScore(question.getScore());
                score += question.getScore();
                correct++;
            }
            list.add(d);
        }
        recordDetailService.saveBatch(list);
        // TODO 3.更新题目数据

        // 4.更新考试记录
        ExamRecord r = new ExamRecord();
        r.setId(recordId);
        r.setScore(score);
        r.setCorrectQuestions(correct);
        r.setDuration((int) DateUtils.between(record.getCreateTime(), LocalDateTime.now()).toSeconds());
        r.setFinishTime(LocalDateTime.now());
        r.setFinished(true);
        updateById(r);

    }

    @Override
    public List<ExamRecordDetailVO> queryDetailsByExamId(Long examId) {
        // 1.查询考试详情
        List<ExamRecordDetail> details = recordDetailService.lambdaQuery()
                .eq(ExamRecordDetail::getExamId, examId)
                .list();
        AssertUtils.isNotEmpty(details, "考试数据不存在");
        // 2.获取问题信息
        List<Long> qIds = details.stream().map(ExamRecordDetail::getQuestionId).collect(Collectors.toList());
        // 3.查询问题
        List<QuestionDTO> questions = questionService.queryQuestionByIds(qIds);
        AssertUtils.isTrue(questions.size() == qIds.size(), "问题不存在");
        Map<Long, QuestionDTO> qMap = questions.stream()
                .collect(Collectors.toMap(QuestionDTO::getId, Function.identity()));
        // 4.组织VO
        List<ExamRecordDetailVO> list = new ArrayList<>(details.size());
        for (ExamRecordDetail detail : details) {
            ExamRecordDetailVO v = BeanUtils.toBean(detail, ExamRecordDetailVO.class);
            v.setQuestion(qMap.get(detail.getQuestionId()));
            list.add(v);
        }
        return list;
    }

}
