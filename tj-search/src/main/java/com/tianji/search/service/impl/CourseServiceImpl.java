package com.tianji.search.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.search.domain.po.Course;
import com.tianji.search.domain.po.SuggestIndex;
import com.tianji.search.repository.CourseRepository;
import com.tianji.search.repository.SuggestIndexRepository;
import com.tianji.search.service.ICourseService;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class CourseServiceImpl implements ICourseService {

    @Resource
    private CourseRepository courseRepository;
    @Resource
    private SuggestIndexRepository suggestIndexRepository;
    @Resource
    private CourseClient courseClient;

    @Override
    public void handleCourseDelete(Long courseId) {
        // 1.直接删除
        courseRepository.deleteById(courseId);
        suggestIndexRepository.deleteById(courseId);
    }

    @Override
    public void handleCourseUp(Long courseId) {
        // 1.根据id查询课程信息
        CourseSearchDTO courseSearchDTO = courseClient.getSearchInfo(courseId);
        if (courseSearchDTO == null) {
            return;
        }
        // 2.数据转换
        Course course = BeanUtils.toBean(courseSearchDTO, Course.class);
        course.setType(courseSearchDTO.getCourseType());
        // 3.写入索引库
        courseRepository.save(course);
        // 4.写入提词库
        saveSuggestDoc(courseSearchDTO);

    }

    @Override
    public void updateCourseSold(List<Long> courseIds, int amount) {
        courseRepository.incrementSold(courseIds, amount);
    }

    @Override
    public void handleCourseDeletes(List<Long> courseIds) {
        // 1.直接删除
        courseRepository.deleteByIds(courseIds);
        suggestIndexRepository.deleteAllById(courseIds);
    }

    //新增提词库索引文档
    @Override
    public void saveSuggestDoc(CourseSearchDTO dto) {
        //处理专辑标题
        String title = dto.getName();
        SuggestIndex suggestIndex = new SuggestIndex();
        suggestIndex.setId(dto.getId());
        suggestIndex.setTitle(title);
        suggestIndex.setKeyword(new Completion(new String[]{title}));
        suggestIndex.setKeywordPinyin(new Completion(new String[]{PinyinUtil.getPinyin(title, "")}));
        suggestIndex.setKeywordSequence(new Completion(new String[]{PinyinUtil.getFirstLetter(title, "")}));

        suggestIndexRepository.save(suggestIndex);
    }

}
