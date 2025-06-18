package com.tianji.chat.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.search.SearchClient;
import com.tianji.api.dto.course.CourseDTO;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.chat.domain.query.CourseQuery;
import com.tianji.chat.domain.vo.CourseVO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ToolsService {

    @Autowired
    private SearchClient searchClient;
    @Autowired
    private CourseClient courseClient;

    @Tool("对给定的 2 个数字求和")
    double sum(double a, double b) {
        return a + b;
    }

    @Tool("返回给定数字的平方根")
    double squareRoot(double x) {
        return Math.sqrt(x);
    }

    @Tool("根据条件查询课程")
    public List<CourseVO> queryCourse(@P(value = "课程名称") String name) {
        List<Long> longs = searchClient.queryCoursesIdByName(name);
        List<CourseSimpleInfoDTO> courseSearchDTOS = courseClient.getSimpleInfoList(longs);
        List<CourseVO> voList = new ArrayList<>();
        for (CourseSimpleInfoDTO courseSearchDTO : courseSearchDTOS) {
            CourseVO courseVO = new CourseVO();
            courseVO.setId(courseSearchDTO.getId());
            courseVO.setName(courseSearchDTO.getName());
            courseVO.setPrice(courseSearchDTO.getPrice());

            voList.add(courseVO);
        }
        log.info("查询课程：{}", courseSearchDTOS);
        return voList;
    }
}
