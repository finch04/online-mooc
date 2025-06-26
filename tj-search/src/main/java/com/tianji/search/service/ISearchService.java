package com.tianji.search.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.search.domain.query.CoursePageQuery;
import com.tianji.search.domain.vo.CourseVO;

import java.util.List;

public interface ISearchService {

    List<CourseVO> queryCourseByCateId(Long cateLv2Id);

    List<CourseVO> queryBestTopN();

    List<CourseVO> queryNewTopN();

    List<CourseVO> queryFreeTopN();

    PageDTO<CourseVO> queryCoursesForPortal(CoursePageQuery query);

    List<Long> queryCoursesIdByName(String keyword);

    List<CourseVO> queryLikeTopN();

    List<String> completeSuggest(String keyword);


    List<String> getSearchHistory();

    void clearSearchHistory();

    void deleteSearchHistory(String keyword);
}
