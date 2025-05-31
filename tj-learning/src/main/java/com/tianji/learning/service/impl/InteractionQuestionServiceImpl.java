package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.search.SearchClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.service.IInteractionQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-23
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {

    private final IInteractionReplyService replyService;
    private final UserClient userClient;
    private final SearchClient searchClient;
    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    private final CategoryCache categoryCache;




    //新增互动问题
    @Override
    public void saveQuestion(QuestionFormDTO dto) {
        Long userId = UserContext.getUser();

        InteractionQuestion question = BeanUtils.copyBean(dto, InteractionQuestion.class);
        question.setUserId(userId);

        this.save(question);

    }

    //修改互动问题
    @Override
    public void updateQuestion(Long id, QuestionFormDTO dto) {


        if(StringUtils.isBlank(dto.getTitle())|| StringUtils.isBlank(dto.getDescription())||dto.getAnonymity()==null){
            throw new BadRequestException("非法参数！");
        }

        InteractionQuestion question = this.getById(id);
        if(question==null){
            throw new BadRequestException("非法参数！");
        }
        //用户只能修改自己的问题
        Long userId =UserContext.getUser();
        if(!question.getUserId().equals(userId)){
            throw new BadRequestException("只能修改自己的互动问题！");
        }

        //dto转po
        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setAnonymity(dto.getAnonymity());
        this.updateById(question);

    }

    //分页查询用户端
    @Override
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        //校验，参数courseId
        if(query.getCourseId()==null){
            throw new BadRequestException("课程id不能为空！");
        }
        //获取用户id
        Long userId =UserContext.getUser();
        //分页查询问题信息 条件：courseId  onlyMine为true时候需要userId 隐藏为false 小节id不为空 分页查询 按提问时间倒序
        Page<InteractionQuestion> page = this.lambdaQuery()
//                .select(InteractionQuestion::getId,InteractionQuestion::getTitle,InteractionQuestion::getCourseId) 此方法可以指定要查的字段
//                .select(InteractionQuestion.class, new Predicate<TableFieldInfo>() {
//                    @Override
//                    public boolean test(TableFieldInfo tableFieldInfo) {
//                        return !tableFieldInfo.getProperty().equals("description");//指定不查的字段
//                    }
//                })
                .select(InteractionQuestion.class,tableFieldInfo -> !tableFieldInfo.getProperty().equals("description"))//简洁写法
                .eq(InteractionQuestion::getCourseId, query.getCourseId())
                .eq(query.getOnlyMine(), InteractionQuestion::getUserId, userId)
                .eq(query.getSectionId() != null, InteractionQuestion::getSectionId, query.getSectionId())
                .eq(InteractionQuestion::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }

        Set<Long> latestAnswerIds=new HashSet<>(); //互动问题的 最新回答id集合
        Set<Long> userIds=new HashSet<>(); //互动问题的用户id集合
        for (InteractionQuestion record : records) {
            if(!record.getAnonymity()){//如果用户是匿名提问，则不显示用户名和头像
                userIds.add(record.getUserId());
            }

            if(record.getLatestAnswerId()!=null){
                latestAnswerIds.add(record.getLatestAnswerId());
            }
        }
//        Set<Long> latestAnswerIds=records.stream()
//                .filter(c->c.getLatestAnswerId()!=null)
//                .map(InteractionQuestion::getLatestAnswerId)
//                .collect(Collectors.toSet());
//                （stream流写法）

        //根据最新回答id，批量查询回答信息
        Map<Long,InteractionReply> replyMap=new HashMap<>();//集合有可能为空，需要做非空校验
        if(CollUtils.isNotEmpty(latestAnswerIds)){
//            List<InteractionReply> replyList = replyService.listByIds(latestAnswerIds);
            List<InteractionReply> replyList = replyService.list(Wrappers.
                    <InteractionReply>lambdaQuery()
                    .in(InteractionReply::getId, latestAnswerIds)
                    .eq(InteractionReply::getHidden, false)
            );
            for (InteractionReply reply : replyList) {
                if(!reply.getAnonymity()){
                    userIds.add(reply.getUserId());//将最新回答的用户id 存入userIds
                }

                replyMap.put(reply.getId(),reply);
            }

        }
        //远程调用用户服务 获取用户信息
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));


        //封装vo
        List<QuestionVO> voList=new ArrayList<>();
        for (InteractionQuestion record : records) {
            QuestionVO vo =BeanUtils.copyBean(record,QuestionVO.class);
            if(!vo.getAnonymity()){
                UserDTO userDTO = userDTOMap.get(record.getUserId());
                if(userDTO!=null){
                    vo.setUserName(userDTO.getName());
                    vo.setUserIcon(userDTO.getIcon());
                }

            }
            InteractionReply reply = replyMap.get(record.getLatestAnswerId());
            if(reply!=null){
                if(!reply.getAnonymity()){
                    UserDTO userDTO = userDTOMap.get(reply.getUserId());
                    if(userDTO!=null){
                        vo.setLatestReplyUser(userDTO.getName() );//最新回答者的昵称
                    }

                }
                vo.setLatestReplyContent(reply.getContent());//最新回答信息
            }

            voList.add(vo);
        }

        return PageDTO.of(page,voList);
    }

    //查询问题详情
    @Override
    public QuestionVO queryQuestionById(Long id) {
        //校验
        if(id==null){
            throw new BadRequestException("非法参数！");
        }
        //查询互动问题表  按主键查询
        InteractionQuestion question = this.getById(id);
        if(question==null){
            throw new BadRequestException("问题不存在！");
        }
        //如果该问题管理员设置了隐藏，则返回空
        if(question.getHidden()){
            return null;
        }
        //封装vo返回
        QuestionVO vo = BeanUtils.copyBean(question, QuestionVO.class);

        //如果用户匿名提问，则不用查询提问者昵称和头像
        if(!question.getAnonymity()){
            UserDTO userDTO = userClient.queryUserById(question.getUserId());
            if(userDTO!=null){
                vo.setUserName(userDTO.getName());
                vo.setUserIcon(userDTO.getIcon());
            }
        }

        return vo;
    }

    //分页查询互动问题-管理端
    @Override
    public PageDTO<QuestionAdminVO> queryQuestionAdminVOPage(QuestionAdminPageQuery query) {
        //如果用户传了课程的名称参数 则从es中获取该名称对应的课程id
        String courseName=query.getCourseName();
        List<Long> cids =null;
        if(StringUtils.isNotBlank(courseName)){
            cids=searchClient.queryCoursesIdByName(courseName);//通过feign远程调用搜索服务，从es查询
            if(CollUtils.isEmpty(cids)){
                return PageDTO.empty(0L,0L);
            }
        }


        //查询互动问题表 条件前端传条件了就添加条件  分页 排序按提问时间排序
        Page<InteractionQuestion> page = this.lambdaQuery()
                .in(CollUtils.isNotEmpty(cids),InteractionQuestion::getCourseId, cids)
                .eq(query.getStatus() != null, InteractionQuestion::getStatus, query.getStatus())
                .between(query.getBeginTime() != null && query.getEndTime() != null,
                        InteractionQuestion::getCreateTime, query.getBeginTime(), query.getEndTime())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(0L,0L);
        }

        Set<Long> uids=new HashSet<>();//用户id集合
        Set<Long> courseids=new HashSet<>();//课程id集合
        Set<Long> chapterAndSections=new HashSet<>();//章和节的id集合
        for (InteractionQuestion record : records) {
            uids.add(record.getUserId());
            courseids.add(record.getCourseId());
            chapterAndSections.add(record.getChapterId());//章id
            chapterAndSections.add(record.getSectionId());//节id
        }
        //远程调用用户服务，获取用户信息
        List<UserDTO> userDTOS = userClient.queryUserByIds(uids);
        if(CollUtils.isEmpty(userDTOS)){
            throw new BizIllegalException("用户不存在");
        }
        Map<Long, UserDTO> userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));

        //远程调用课程服务，获取课程信息
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseids);
        if(CollUtils.isEmpty(cinfos)){
            throw new BizIllegalException("课程不存在");
        }
        Map<Long, CourseSimpleInfoDTO> cinfoMap = cinfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        //远程调用课程服务，获取章节信息
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(chapterAndSections);
        if(CollUtils.isEmpty(cataSimpleInfoDTOS)) {
            throw new BizIllegalException("章节信息不存在");
        }
        Map<Long, String> cataInfoDTO = cataSimpleInfoDTOS.stream().collect(Collectors.toMap(CataSimpleInfoDTO::getId, c -> c.getName()));



        //封装vo并返回
        List<QuestionAdminVO> voList=new ArrayList<>();
        for (InteractionQuestion record : records) {
            QuestionAdminVO adminVO=BeanUtils.copyBean(record,QuestionAdminVO.class);
            UserDTO userDTO = userDTOMap.get(record.getUserId());
            if(userDTO!=null){
                adminVO.setUserName(userDTO.getName());
            }
            CourseSimpleInfoDTO cinfoDTO = cinfoMap.get(record.getCourseId());
            if(cinfoDTO!=null){
                adminVO.setCourseName(cinfoDTO.getName());
                List<Long> categoryIds = cinfoDTO.getCategoryIds();//一二三级分类id集合
                // 获取课程一二三级分类id
                String categoryNames = categoryCache.getCategoryNames(categoryIds);
                adminVO.setCategoryName(categoryNames);//三级分类名称 拼接字段
            }

            adminVO.setChapterName(cataInfoDTO.get(record.getChapterId()));//章名称
            adminVO.setSectionName(cataInfoDTO.get(record.getSectionId()));//节名称


            voList.add(adminVO);

        }

        return PageDTO.of(page,voList);
    }

    //用户删除自己的问题
    @Override
    public void deleteQuestion(Long id) {
//                -查询问题是否存在
        if(id==null){
            throw new BadRequestException("非法参数！");
        }
        InteractionQuestion question = this.getById(id);
        if(question==null){
            throw new BadRequestException("问题不存在！");
        }
//                - 判断是否是当前用户提问的
        Long userId =UserContext.getUser();
//                - 如果不是则报错
        if(!question.getUserId().equals(userId)){
            throw new BadRequestException("只能修改自己的互动问题！");
        }

//                - 如果是则删除问题
        this.removeById(id);
//                - 然后删除问题下的回答及评论
        replyService.remove(Wrappers.<InteractionReply>lambdaQuery().eq(InteractionReply::getQuestionId,id));

    }

    //管理端隐藏或显示问题
    @Override
    public void hiddenQuestion(Long id, boolean hidden) {
        if(id==null){
            throw new BadRequestException("非法参数！");
        }
        InteractionQuestion question = this.getById(id);
        if(question==null){
            throw new BadRequestException("问题不存在！");
        }
        question.setHidden(hidden);
        this.updateById(question);

    }

    //管理端根据id查询问题详情
    @Override
    public QuestionAdminVO queryQuestionAdminById(Long id) {
        //我自己写的：
//        if(id==null){
//            throw new BadRequestException("非法参数");
//        }
//        InteractionQuestion question = this.getById(id);
//
//        if(question==null){
//            throw new BadRequestException("问题不存在");
//        }
////        CourseFullInfoDTO courseInfoById = courseClient.getCourseInfoById(question.getChapterId(),false,false);
//        QuestionAdminVO adminVO = BeanUtils.copyBean(question, QuestionAdminVO.class);
//        CourseSearchDTO searchInfo = courseClient.getSearchInfo(question.getCourseId());
//        UserDTO userDTO = userClient.queryUserById(searchInfo.getTeacher());
//        UserDTO dto = userClient.queryUserById(question.getUserId());
////        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(courseInfoById.getCategoryIds());
////
////        if(CollUtils.isEmpty(cataSimpleInfoDTOS)) {
////            throw new BizIllegalException("章节信息不存在");
////        }
////        Map<Long, String> cataInfoDTO = cataSimpleInfoDTOS.stream().collect(Collectors.toMap(CataSimpleInfoDTO::getId, c -> c.getName()));
//
//        //封装
//        adminVO.setTeacherName(userDTO.getName());
//        adminVO.setAnswerTimes(question.getAnswerTimes());
//        adminVO.setCourseName(searchInfo.getName());
//        adminVO.setUserIcon(dto.getIcon());
//        adminVO.setStatus(1);
//        this.updateById(question.setStatus(QuestionStatus.CHECKED));
////        adminVO.setCategoryName(cataInfoDTO.get(question.getSectionId()));
//
//        return adminVO;
        //标准答案：
        // 1.根据id查询问题
        InteractionQuestion question = getById(id);
        if (question == null) {
            return null;
        }
        // 2.转PO为VO
        QuestionAdminVO vo = BeanUtils.copyBean(question, QuestionAdminVO.class);
        // 3.查询提问者信息
        UserDTO user = userClient.queryUserById(question.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserIcon(user.getIcon());
        }
        // 4.查询课程信息
        CourseFullInfoDTO cInfo = courseClient.getCourseInfoById(
                question.getCourseId(), false, true);
        if (cInfo != null) {
            // 4.1.课程名称信息
            vo.setCourseName(cInfo.getName());
            // 4.2.分类信息
            vo.setCategoryName(categoryCache.getCategoryNames(cInfo.getCategoryIds()));
            // 4.3.教师信息
            List<Long> teacherIds = cInfo.getTeacherIds();
            List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
            if(CollUtils.isNotEmpty(teachers)) {
                vo.setTeacherName(teachers.stream()
                        .map(UserDTO::getName).collect(Collectors.joining("/")));
            }
        }
        // 5.查询章节信息
        List<CataSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(
                List.of(question.getChapterId(), question.getSectionId()));
        Map<Long, String> cataMap = new HashMap<>(catas.size());
        if (CollUtils.isNotEmpty(catas)) {
            cataMap = catas.stream()
                    .collect(Collectors.toMap(CataSimpleInfoDTO::getId, CataSimpleInfoDTO::getName));
        }
        vo.setChapterName(cataMap.getOrDefault(question.getChapterId(), ""));
        vo.setSectionName(cataMap.getOrDefault(question.getSectionId(), ""));
        // 6.封装VO
        //更新为已查看
        this.updateById(question.setStatus(QuestionStatus.CHECKED));
        vo.setAnswerTimes(question.getAnswerTimes());
        return vo;
    }
}
