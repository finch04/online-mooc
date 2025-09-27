package com.tianji.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.ShardFailure;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.json.JsonData;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.constants.ErrorInfo;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.AssertUtils;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.search.config.InterestsProperties;
import com.tianji.search.constants.SearchErrorInfo;
import com.tianji.search.domain.po.Course;
import com.tianji.search.domain.query.CoursePageQuery;
import com.tianji.search.domain.vo.CourseVO;
import com.tianji.search.repository.CourseRepository;
import com.tianji.search.service.IInterestsService;
import com.tianji.search.service.ISearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.search.repository.CourseRepository.PUBLISH_TIME;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    private final ElasticsearchClient esClient;
    private final IInterestsService interestsService;
    private final UserClient userClient;
    private final CategoryCache categoryCache;
    private final InterestsProperties interestsProperties;

    @Override
    public List<CourseVO> queryCourseByCateId(Long cateLv2Id) {
        return queryTopNByCategoryIdLv2sAndFree(
                CollUtils.singletonList(cateLv2Id), null, PUBLISH_TIME+".keyword", false, 10);
    }

    @Override
    public List<CourseVO> queryBestTopN() {
        return queryTopNCourseOnMarketByFree(false, CourseRepository.SOLD);
    }

    @Override
    public List<CourseVO> queryNewTopN() {
        return queryTopNCourseOnMarketByFree(false, PUBLISH_TIME+".keyword");
    }

    @Override
    public List<CourseVO> queryFreeTopN() {
        return queryTopNCourseOnMarketByFree(true, CourseRepository.SOLD);
    }

    private List<CourseVO> queryTopNCourseOnMarketByFree(boolean isFree, String sortBy) {
        Long userId = UserContext.getUser();
        List<CourseVO> courses;

        if (userId == null) {
            // 未登录用户查询
            courses = queryTopNByCategoryIdLv2sAndFree(
                    null, isFree, sortBy, false, interestsProperties.getTopNumber());
        } else {
            // 已登录用户查询
            List<Long> categoryIds = interestsService.queryMyInterestsIds();
            if (CollUtils.isEmpty(categoryIds)) {
                courses = queryTopNByCategoryIdLv2sAndFree(
                        null, isFree, sortBy, false, interestsProperties.getTopNumber());
            } else {
                courses = queryTopNByCategoryIdLv2sAndFree(
                        categoryIds, isFree, sortBy, false, interestsProperties.getTopNumber());
            }
        }
        return courses;
    }

    private List<CourseVO> queryTopNByCategoryIdLv2sAndFree(
            List<Long> categoryIds, Boolean isFree, String sortBy, boolean isASC, int n) {
        try {
            // 构建查询条件
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // 过滤条件：是否免费
            if (isFree != null) {
                boolQuery.filter(f -> f.term(t -> t
                        .field(CourseRepository.FREE)
                        .value(isFree)));
            }

            // 过滤条件：分类ID
            if (CollUtils.isNotEmpty(categoryIds)) {
                boolQuery.filter(f -> f.terms(t -> t
                        .field(CourseRepository.CATEGORY_ID_LV2)
                        .terms(ts -> ts.value(categoryIds.stream()
                                .map(id -> FieldValue.of(id))
                                .collect(Collectors.toList())))));
            }

            // 构建搜索请求
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index(CourseRepository.INDEX_NAME)
                    .query(boolQuery.build()._toQuery())
                    .size(Math.min(n, 1000)); // 限制最大返回数量，避免过大查询

            // 添加排序条件（如果sortBy不为空且是有效的排序字段）
            if (StringUtils.isNotBlank(sortBy)) {
                // 验证排序字段是否存在于映射中（可选，但推荐）
                // 这里可以添加对sortBy字段的验证逻辑
                requestBuilder.sort(s -> s.field(f -> f
                        .field(sortBy)
                        .order(isASC ? SortOrder.Asc : SortOrder.Desc)
                ));
            } else {
                // 如果没有指定排序，添加默认排序（比如按_id排序）以避免潜在问题
                requestBuilder.sort(s -> s.score(sb -> sb.order(SortOrder.Desc)));
            }

            // 执行查询
            SearchResponse<Course> response = esClient.search(requestBuilder.build(), Course.class);

            // 检查查询是否成功
            if (response.shards() != null && response.shards().failures() != null) {
                for (ShardFailure failure : response.shards().failures()) {
                    log.error("Shard failure: {}", failure.reason());
                }
            }

            // 解析结果
            List<Hit<Course>> hits = response.hits().hits();
            if (CollUtils.isEmpty(hits)) {
                return CollUtils.emptyList();
            }

            List<CourseVO> courses = new ArrayList<>(hits.size());
            Set<Long> teacherIds = new HashSet<>(hits.size());

            for (Hit<Course> hit : hits) {
                Course course = hit.source();
                if (course == null) {
                    continue;
                }

                CourseVO vo = BeanUtils.toBean(course, CourseVO.class);
                teacherIds.add(course.getTeacher());
                courses.add(vo);
            }

            // 补充教师信息
            teacherIds.remove(0L);
            if (CollUtils.isNotEmpty(teacherIds)) {
                List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
                AssertUtils.isNotEmpty(teachers, SearchErrorInfo.TEACHER_NOT_EXISTS);

                Map<Long, String> teacherMap = teachers.stream()
                        .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));

                courses.forEach(vo -> vo.setTeacher(teacherMap.getOrDefault(
                        Long.valueOf(vo.getTeacher()), "匿名")));
            }

            return courses;
        } catch (IOException e) {
            log.error("Elasticsearch query failed: {}", e.getMessage(), e);
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        } catch (Exception e) {
            log.error("Unexpected error during Elasticsearch query: {}", e.getMessage(), e);
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        }
    }

    @Override
    public PageDTO<CourseVO> queryCoursesForPortal(CoursePageQuery query) {
        try {
            // 1.执行搜索
            SearchResponse<Course> response = searchForResponse(query, CourseVO.EXCLUDE_FIELDS);

            // 2.处理响应结果
            PageDTO<Course> result = handleSearchResponse(response, query.getPageSize());

            // 3.转换为VO
            List<Course> courses = result.getList();
            if (CollUtils.isEmpty(courses)) {
                return PageDTO.empty(result.getTotal(), result.getPages());
            }

            // 补充教师信息
            List<Long> teacherIds = courses.stream()
                    .map(Course::getTeacher)
                    .collect(Collectors.toList());

            List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
            AssertUtils.isNotEmpty(teachers, SearchErrorInfo.TEACHER_NOT_EXISTS);

            Map<Long, String> teacherMap = teachers.stream()
                    .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));

            List<CourseVO> vos = courses.stream()
                    .map(course -> {
                        CourseVO vo = BeanUtils.toBean(course, CourseVO.class);
                        vo.setTeacher(teacherMap.getOrDefault(course.getTeacher(), "未知"));
                        return vo;
                    })
                    .collect(Collectors.toList());

            return new PageDTO<>(result.getTotal(), result.getPages(), vos);
        } catch (Exception e) {
            throw new CommonException(ErrorInfo.Msg.SERVER_INTER_ERROR, e);
        }
    }

    @Override
    public List<Long> queryCoursesIdByName(String keyword) {
        try {
            // 构建查询
            SearchRequest request = SearchRequest.of(s -> s
                    .index(CourseRepository.INDEX_NAME)
                    .query(q -> q
                            .matchPhrase(mp -> mp
                                    .field(CourseRepository.DEFAULT_QUERY_NAME)
                                    .query(keyword)
                            )
                    )
                    .source(sc -> sc
                            .filter(f -> f.includes("id"))
                    )
            );

            // 执行查询
            SearchResponse<Course> response = esClient.search(request, Course.class);

            // 解析结果
            return response.hits().hits().stream()
                    .map(hit -> hit.source().getId())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        }
    }

    private SearchResponse<Course> searchForResponse(CoursePageQuery query, String[] excludeFields) throws IOException {
        // 构建基础查询
        BoolQuery.Builder boolQuery = buildBasicQuery(query);


        // 构建搜索请求（使用 8.x 版本的 Builder 模式）
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(CourseRepository.INDEX_NAME)
                .query(boolQuery.build()._toQuery())
                .from(query.from())
                .size(query.getPageSize());

        // 排序（8.x 版本需通过 sort 方法的函数式接口配置）
        if (StringUtils.isNotBlank(query.getSortBy())) {
            if("publishTime".equals(query.getSortBy())){
                query.setSortBy("publishTime.keyword");
            }
            requestBuilder.sort(sort -> sort
                    .field(field -> field
                            .field(query.getSortBy())
                            .order(query.getIsAsc() ? SortOrder.Asc : SortOrder.Desc)
                    )
            );
        }

        // 高亮设置（8.x 版本通过 fields 方法配置字段高亮）
        requestBuilder.highlight(highlight -> highlight
                .fields(CourseRepository.DEFAULT_QUERY_NAME, field -> field
                        .preTags("<em>")
                        .postTags("</em>")
                )
        );

        // 过滤返回字段（8.x 版本 source 配置语法）
        if (excludeFields != null && excludeFields.length > 0) {
            requestBuilder.source(source -> source
                    .filter(f -> f.excludes(Arrays.asList(excludeFields)))
            );
        }

        // 执行查询时构建请求
        SearchRequest request = requestBuilder.build();

        // 使用 ElasticsearchClient 执行查询并返回结果
        return esClient.search(request, Course.class);
    }

    private BoolQuery.Builder buildBasicQuery(CoursePageQuery query) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 关键字搜索
        String keyword = query.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            boolQuery.must(must -> must.matchAll(m -> m));
        } else {
            boolQuery.must(must -> must.matchPhrase(mp -> mp
                    .field(CourseRepository.DEFAULT_QUERY_NAME)
                    .query(keyword)
            ));
        }

        // 分类过滤
        if (query.getCategoryIdLv1() != null) {
            boolQuery.filter(filter -> filter.term(t -> t
                    .field(CourseRepository.CATEGORY_ID_LV1)
                    .value(query.getCategoryIdLv1())
            ));
        }
        if (query.getCategoryIdLv2() != null) {
            boolQuery.filter(filter -> filter.term(t -> t
                    .field(CourseRepository.CATEGORY_ID_LV2)
                    .value(query.getCategoryIdLv2())
            ));
        }
        if (query.getCategoryIdLv3() != null) {
            boolQuery.filter(filter -> filter.term(t -> t
                    .field(CourseRepository.CATEGORY_ID_LV3)
                    .value(query.getCategoryIdLv3())
            ));
        }

        // 其他过滤条件
        if (query.getFree() != null) {
            boolQuery.filter(filter -> filter.term(t -> t
                    .field(CourseRepository.FREE)
                    .value(query.getFree())
            ));
        }
        if (query.getType() != null) {
            boolQuery.filter(filter -> filter.term(t -> t
                    .field(CourseRepository.TYPE)
                    .value(query.getType())
            ));
        }

        // 时间范围过滤
        LocalDateTime beginTime = query.getBeginTime();
        LocalDateTime endTime = query.getEndTime();
        if (beginTime != null || endTime != null) {
            boolQuery.filter(filter -> filter.range(r -> {
                RangeQuery.Builder range = new RangeQuery.Builder().field(CourseRepository.UPDATE_TIME);
                if (beginTime != null) {
                    range.gte(JsonData.of(beginTime));
                }
                if (endTime != null) {
                    range.lte(JsonData.of(endTime));
                }
                return range;
            }));
        }

        return boolQuery;
    }
    private PageDTO<Course> handleSearchResponse(SearchResponse<Course> response, int pageSize) {
        // 总条数
        long total = response.hits().total().value();
        // 总页数
        long totalPages = (total + pageSize - 1) / pageSize;

        // 处理命中数据
        List<Hit<Course>> hits = response.hits().hits();
        if (CollUtils.isEmpty(hits)) {
            return new PageDTO<>(total, totalPages, CollUtils.emptyList());
        }

        List<Course> courses = new ArrayList<>(hits.size());
        for (Hit<Course> hit : hits) {
            Course course = hit.source();
            if (course == null) {
                continue;
            }

            // 处理高亮
            if (hit.highlight() != null && hit.highlight().containsKey(CourseRepository.DEFAULT_QUERY_NAME)) {
                List<String> highlights = hit.highlight().get(CourseRepository.DEFAULT_QUERY_NAME);
                if (CollUtils.isNotEmpty(highlights)) {
                    course.setName(highlights.get(0));
                }
            }

            courses.add(course);
        }

        return new PageDTO<>(total, totalPages, courses);
    }
}