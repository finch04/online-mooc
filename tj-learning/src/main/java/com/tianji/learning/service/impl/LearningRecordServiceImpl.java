package com.tianji.learning.service.impl;

import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.SectionType;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.utils.LearningRecordDelayTaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.Trace;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 学习记录表 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {

    private final ILearningLessonService lessonService;
    private final  CourseClient courseClient;

    private final LearningRecordDelayTaskHandler taskHandler;
    private final RabbitMqHelper mqHelper;


    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        //获取当前用户id
        Long userId = UserContext.getUser();

        //查询课表信息 条件user_id和courseId
        LearningLesson lesson = lessonService.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson==null){
            throw new BadRequestException("该课程未加入课表！");
        }
        //查询学习记录 条件lesson_id和user_id
        List<LearningRecord> recordList = this.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getLessonId, lesson.getId())
                .list();
        //封装成dto
        LearningLessonDTO dto =new LearningLessonDTO();
        List<LearningRecordDTO> dtoList = BeanUtils.copyList(recordList, LearningRecordDTO.class);
        dto.setRecords(dtoList);
        dto.setId(lesson.getId());
        dto.setLatestSectionId(lesson.getLatestSectionId());


        return dto;
    }

    //提交学习记录
    @Override
    public void addLearningRecord(LearningRecordFormDTO dto) {
        //1.获取当前用户id
        Long userId =UserContext.getUser();
        boolean isFinished=false;//代表本
        //2.处理学习记录
        if(dto.getSectionType().equals(SectionType.VIDEO)){
            //2.1提交考试记录
            isFinished=handleVideoRecord(userId,dto);
        }else{
            //2.2提交视频播放记录
            isFinished=handleExamRecord(userId,dto);
        }
        //3.处理课表数据
        if(!isFinished){//如果本小节不是第一次学完，不用处理课表数据
            return;
        }

        handleLessonData(dto);

    }

    //处理课表相关数据
    private void handleLessonData(LearningRecordFormDTO dto) {
        //查询课表 learning_lesson 主键
         LearningLesson lesson = lessonService.getById(dto.getLessonId());
        if(lesson==null){
            throw new BizIllegalException("课表不存在！");
        }

        boolean allFinished= false; //所有小节是否已学完
        //判断是否是第一次学完 isFinished

        //远程调用课程服务，得到课程信息，小节总数
        CourseFullInfoDTO cinfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if(cinfo==null){
            throw new BizIllegalException("课程不存在！");
        }
        Integer sectionNum = cinfo.getSectionNum();
        //判断该用户在小节下全部学完
        Integer learnedSections = lesson.getLearnedSections();
        allFinished=learnedSections+1>=sectionNum;

        //更新课表数据
        lessonService.lambdaUpdate()
//                .set(lesson.getStatus()==LessonStatus.NOT_BEGIN,LearningLesson::getStatus,LessonStatus.LEARNING)
                .set(lesson.getLearnedSections()==0,LearningLesson::getStatus,LessonStatus.LEARNING.getValue())
                .set(allFinished,LearningLesson::getStatus, LessonStatus.FINISHED)
                .set(LearningLesson::getLatestSectionId,dto.getSectionId())
                .set(LearningLesson::getLatestLearnTime,dto.getCommitTime())
//                .set(isFinished,LearningLesson::getLearnedSections,lesson.getLearnedSections()+1)
                .setSql( "learned_sections = learned_sections + 1")
                .eq(LearningLesson::getId, lesson.getId())
                .update();



    }

    //处理视频播放记录
    private boolean handleVideoRecord(Long userId, LearningRecordFormDTO dto) {

        LearningRecord learningRecord= queryOldRecord(dto.getLessonId(),dto.getSectionId());

        //查询旧的学习记录 learning_record 条件userId lessonId section_id
//        LearningRecord learningRecord= this.lambdaQuery()
//                .eq(LearningRecord::getLessonId, dto.getLessonId())
//                .eq(LearningRecord::getSectionId, dto.getSectionId()).one();
        //判断是否存在
        if(learningRecord==null){
            //如果不存在，则新增学习记录
            LearningRecord record = BeanUtils.copyBean(dto, LearningRecord.class);
            record.setUserId(userId);
            boolean result = this.save(record);
            if(!result){
                throw new DbException("新增考试记录失败！");
            }
            return false;//代表本小节没有学完
        }
        //如果存在则更新学习记录learning_record  moment finish_time
        //判断本小节是否为第一次学完
        boolean isFinished = !learningRecord.getFinished() && dto.getMoment()*2>=dto.getDuration();

        if(!isFinished){
            LearningRecord record =new LearningRecord();
            record.setLessonId(dto.getLessonId());
            record.setSectionId(dto.getSectionId());
            record.setMoment(dto.getMoment());
            record.setFinished(learningRecord.getFinished());
            record.setId(learningRecord.getId());
            taskHandler.addLearningRecordTask(record);
            return false;
        }

        boolean result = this.lambdaUpdate()
                .set(LearningRecord::getMoment, dto.getMoment())
                .set(isFinished, LearningRecord::getFinished, true)//lambdaUpdate传三个参数时，如果第一个参数为真则执行更新
                .set(isFinished, LearningRecord::getFinishTime, dto.getCommitTime())
                .eq(LearningRecord::getId, learningRecord.getId())
                .update();
        if(!result){
            throw new DbException("更新视频学习记录失败！");
        }

        //清理redis缓存
        taskHandler.cleanRecordCache(dto.getLessonId(),dto.getSectionId());
        // 学完一节累加积分
        mqHelper.send(
                MqConstants.Exchange.LEARNING_EXCHANGE,
                MqConstants.Key.LEARN_SECTION,
                10);

        return true;
    }

    private LearningRecord queryOldRecord(Long lessonId, Long sectionId) {
        //查询缓存
        LearningRecord cache = taskHandler.readRecordCache(lessonId, sectionId);
        if(cache!=null){
            //如果命中直接返回
            return cache;
        }
        //如果未命中，查数据库
        LearningRecord dbRecord= this.lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .eq(LearningRecord::getSectionId, sectionId)
                .one();
        if(dbRecord==null){//不写会报空指针
            return null;
        }
        //放入缓存
        taskHandler.writeRecordCache(dbRecord);
        return dbRecord;
    }

    //处理考试记录
    private boolean handleExamRecord(Long userId, LearningRecordFormDTO dto) {
        //将dto转换为po
        LearningRecord record = BeanUtils.copyBean(dto, LearningRecord.class);
        record.setFinished(true);//提交考试记录，代表本小节已经学完
        record.setFinishTime(dto.getCommitTime());
        record.setUserId(userId);
        //保存学习记录learning_records
        boolean result = this.save(record);
        if(!result){
            throw new DbException("新增考试记录失败！");
        }

        return true;
    }
}
