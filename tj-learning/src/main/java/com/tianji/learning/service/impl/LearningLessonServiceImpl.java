package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningLessonMapper;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生课程表 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {

    final CourseClient courseClient;
    final CatalogueClient catalogueClient;
    final LearningRecordMapper recordMapper;

    //添加课程
    @Override
    public void addUserLesson(Long userId, List<Long> courseIds) {
        //通过feign远程调用课程服务，得到课程信息
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        List<LearningLesson> list=new ArrayList<>();
        //封装po实体类，添加过期时间
        for (CourseSimpleInfoDTO cinfo : cinfos) {
            LearningLesson lesson=new LearningLesson();
            lesson.setUserId(userId);
            lesson.setCourseId(cinfo.getId());

            Integer validDuration = cinfo.getValidDuration(); //获取课程有效期 单位：月
            if(validDuration!=null){
                LocalDateTime now =LocalDateTime.now();
                lesson.setCreateTime(now);
                lesson.setExpireTime(now.plusMonths(validDuration));
            }
            list.add(lesson);
        }
        //批量保存
        this.saveBatch(list);
    }

    //分页查询课程
    @Override
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        //获取当前登录人
        Long userId = UserContext.getUser();

//        if(userId==null){
//            throw new BadRequestException("请先登录！");
//        }

        //分页查询我的课表
        Page<LearningLesson> page = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .page(query.toMpPage("latest_learn_time",false));
        List<LearningLesson> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //远程调用课程服务，给vo的课程数，封面，章节数赋值
        Set<Long> courseIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        if(CollUtils.isEmpty(cinfos)){
            throw new BadRequestException("课程不存在");
        }
        //将cinfos课程集合转换为map结构<课程id,对象>
        Map<Long, CourseSimpleInfoDTO> infoDTOMap = cinfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        List<LearningLessonVO> voList=new ArrayList<>();
        //将po中的数据封装到vo中
        for (LearningLesson record : records) {
            LearningLessonVO vo = BeanUtils.copyBean(record, LearningLessonVO.class);
            CourseSimpleInfoDTO infoDTO = infoDTOMap.get(record.getCourseId());
            if(infoDTO!=null){
                vo.setCourseName(infoDTO.getName());
                vo.setCourseCoverUrl(infoDTO.getCoverUrl());
                vo.setSections(infoDTO.getSectionNum());
            }
            voList.add(vo);
        }
        //返回
        return PageDTO.of(page,voList);
    }

    //查询正在学习的课程
    @Override
    public LearningLessonVO queryMyCurrentLesson() {
        //获取当前登录用户id
        Long userId =UserContext.getUser();
        //查询当前用户最近学习课程 按latest_learn_time 降序排序 取第一天 正在学习中的 status=1
        LearningLesson lesson = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getStatus, LessonStatus.LEARNING)
                .orderByDesc(LearningLesson::getLatestLearnTime)
                .last("limit 1")
                .one();

        if(lesson == null){
            return null;
        }
        //远程调用课程服务，给vo中的课程名，封面，章节数赋值
        CourseFullInfoDTO cinfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if(cinfo == null){
            throw new BadRequestException("课程不存在！");
        }
        //查询当前用户课表中，总的课程数
        Integer count = this.lambdaQuery().eq(LearningLesson::getUserId, userId).count();

        //通过feign远程调用课程服务 获取小节名称 和小节编号
        Long latestSectionId = lesson.getLatestSectionId();
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(CollUtils.singletonList(latestSectionId));
        if(cataSimpleInfoDTOS.isEmpty()){
            throw new BadRequestException("小节不存在！");
        }

        //封装到vo返回
        LearningLessonVO vo =BeanUtils.copyBean(lesson, LearningLessonVO.class);
        vo.setCourseName(cinfo.getName());
        vo.setCourseCoverUrl(cinfo.getCoverUrl());
        vo.setSections(cinfo.getSectionNum());
        vo.setCourseAmount(count);
        CataSimpleInfoDTO cataSimpleInfoDTO = cataSimpleInfoDTOS.get(0);
        vo.setLatestSectionName(cataSimpleInfoDTO.getName());
        vo.setLatestSectionIndex(cataSimpleInfoDTO.getCIndex());

        return vo;
    }

    //查询用户课表中指定课程状态
    @Override
    public LearningLessonVO queryLessonByCourseId(Long courseId) {
        //获取当前登录用户id
        Long userId =UserContext.getUser();
        LearningLesson lesson = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson==null){
            return null;
        }
        return BeanUtils.copyBean(lesson, LearningLessonVO.class);
    }

    //检查课程是否有效
    @Override
    public Long isLessonValid(Long courseId) {
        //获取当前登录用户id
        Long userId =UserContext.getUser();
        //查询课表learning_lesson
        LearningLesson lesson=this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson==null){
            return null;
        }
        //校验课程是否过期
        LocalDateTime expireTime = lesson.getExpireTime();
        LocalDateTime now =LocalDateTime.now();
        if(expireTime!=null && now.isAfter(expireTime)){
            return null;
        }
        return lesson.getId();
    }

    //统计课程学习人数
    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        //查询课表learning_lesson
        return this.lambdaQuery()
                .eq(LearningLesson::getCourseId, courseId)
                .count();
    }

    //创建学习计划
    @Override
    public void createLearningPlan(LearningPlanDTO dto) {
        //获取当前登录用户
        Long userId = UserContext.getUser();
        //查询课表
        LearningLesson lesson=this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, dto.getCourseId())
                .one();
        if(lesson==null){
            throw new BizIllegalException("该课程没有加入到课表");
        }

        //链式编程
        this.lambdaUpdate()
                .set(LearningLesson::getWeekFreq,dto.getFreq())
                .set(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .eq(LearningLesson::getId,lesson.getId())
                .update();

    }

    //查询学习计划进度
    @Override
    public LearningPlanPageVO queryMyPlans(PageQuery query) {
        Long userId = UserContext.getUser();
        //查询积分 todo

        //查询本周学习计划的总数据--mybatisplus实现
        QueryWrapper<LearningLesson> wrapper=new QueryWrapper<>();
        wrapper.select("sum(week_freq) as plansTotal");
        wrapper.eq("user_id",userId);
        wrapper.in("status",LessonStatus.LEARNING,LessonStatus.NOT_BEGIN);
        wrapper.eq("plan_status",PlanStatus.PLAN_RUNNING);
        Map<String, Object> map = this.getMap(wrapper);
        Integer plansTotal =0;
        if(map!=null && map.get("plansTotal")!=null){
             plansTotal = Integer.valueOf(map.get("plansTotal").toString());
        }

        //查询本周  实际已学完的计划总数据 learning-record 条件userId finished_time 本周 finished
        LocalDate now =LocalDate.now();
        LocalDateTime weekBeginTime = DateUtils.getWeekBeginTime(now);
        LocalDateTime weekEndTime = DateUtils.getWeekEndTime(now);
        Integer weekFinishedPlanNum = recordMapper.selectCount(Wrappers.<LearningRecord>lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getFinished, true)
                .between(LearningRecord::getFinishTime, weekBeginTime, weekEndTime)
        );
        //查询课表 learning-lessons
        Page<LearningLesson> page = this.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .in(LearningLesson::getStatus, LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            LearningPlanPageVO vo =new LearningPlanPageVO();
            vo.setTotal(0L);
            vo.setPages(0L);
            vo.setList(CollUtils.emptyList());
            return vo;
        }

        //远程调用课程服务，查询课程信息
        Set<Long> courseIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        if(CollUtils.isEmpty(cinfos)){
            throw new BizIllegalException("课程不存在！");
        }
        //将cinfos list结构转为map <课程id,CourseSimpleInfoDTO>
        Map<Long, CourseSimpleInfoDTO> cinfosMap = cinfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        //查询学习记录表 本周 当前用户下 学完的小节数
        QueryWrapper<LearningRecord> rWrapper=new QueryWrapper<>();
        rWrapper.select("lesson_id as lessonId","count(*) as userId");//可以先通过别的字段暂存数据，而无需新建一个字段
        rWrapper.eq("user_id",userId);
        rWrapper.eq("finished",true);
        rWrapper.between("finish_time",weekBeginTime,weekEndTime);
        rWrapper.groupBy("lesson_id");
        List<LearningRecord> learningRecords = recordMapper.selectList(rWrapper);
        //map中的key是lessonId  value是当前用户对该课程下已学习的小节数量
        Map<Long, Long> courseWeekFinishNumMap = learningRecords.stream().collect(Collectors.toMap(LearningRecord::getLessonId, c -> c.getUserId()));//此处的userId就是count
        //封装vo返回
        LearningPlanPageVO vo=new LearningPlanPageVO();
        vo.setWeekTotalPlan(plansTotal);
        vo.setWeekFinished(weekFinishedPlanNum);
        List<LearningPlanVO> voList=new ArrayList<>();

        for (LearningLesson record : records) {
            LearningPlanVO planVO = BeanUtils.copyBean(record, LearningPlanVO.class);
            CourseSimpleInfoDTO infoDTO = cinfosMap.get(record.getCourseId());
            if(infoDTO!=null){
                planVO.setCourseName(infoDTO.getName());//课程名字
                planVO.setSections(infoDTO.getSectionNum());//课程总小节数
            }
//            Long aLong = courseWeekFinishNumMap.get(record.getId());
//            if(aLong!=null){
//                planVO.setWeekLearnedSections(aLong.intValue());
//            }else{
//                planVO.setWeekLearnedSections(0);
//            }
            planVO.setWeekLearnedSections(courseWeekFinishNumMap.getOrDefault(record.getId(),0L).intValue());



            voList.add(planVO);
        }
        vo.setList(voList);
        vo.setTotal(page.getTotal());
        vo.setPages(page.getPages());
        return vo;
    }

    @Override
    public void deleteMyLessons(Long id) {
        Long userId = UserContext.getUser();
        LearningLesson lesson = getById(id);
        if(!lesson.getUserId().equals(userId)){
            throw new BizIllegalException("只能删除自己的课程！");
        }
        if(lesson.getStatus()!=LessonStatus.EXPIRED){
            throw new BizIllegalException("只能删除状态为已过期的课程!");
        }

        baseMapper.deleteById(id);
    }
}
