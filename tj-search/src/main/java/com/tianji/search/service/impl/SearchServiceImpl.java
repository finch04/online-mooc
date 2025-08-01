package com.tianji.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.data.RecommendClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.constants.ErrorInfo;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.*;
import com.tianji.search.config.InterestsProperties;
import com.tianji.search.constants.SearchErrorInfo;
import com.tianji.search.domain.po.Course;
import com.tianji.search.domain.po.SuggestIndex;
import com.tianji.search.domain.query.CoursePageQuery;
import com.tianji.search.domain.vo.CourseVO;
import com.tianji.search.repository.CourseRepository;
import com.tianji.search.service.IInterestsService;
import com.tianji.search.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.FuzzyOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.search.constants.RedisConstants.*;
import static com.tianji.search.repository.CourseRepository.PUBLISH_TIME;
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    @Autowired
    private RestHighLevelClient restClient;

    @Autowired
    private IInterestsService interestsService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private CategoryCache categoryCache;

    @Autowired
    private InterestsProperties interestsProperties;

    @Autowired
    private RecommendClient recommendClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<CourseVO> queryCourseByCateId(Long cateLv2Id) {
        return queryTopNByCategoryIdLv2sAndFree(
                CollUtils.singletonList(cateLv2Id), null, PUBLISH_TIME, false, 10);
    }

    @Override
    public List<CourseVO> queryLikeTopN() {
        List<CourseVO> courseVOS = null;
        if(UserContext.getUser() == null){
            courseVOS = queryTopNCourseOnMarketByFree(false, CourseRepository.TYPE);
        }else{
            List<Long> courseIds = recommendClient.featureRecommend();
            // 根据课程id返回课程VO
            if (CollUtils.isNotEmpty(courseIds)) {
                try {
                    // 使用Elasticsearch查询课程
                    courseVOS = queryCoursesByIds(courseIds);
                } catch (IOException e) {
                    log.error("查询推荐课程失败", e);
                    // 查询失败时使用默认推荐
                    courseVOS = queryTopNCourseOnMarketByFree(false, CourseRepository.TYPE);
                }
            }
            // 如果没有推荐结果，使用默认推荐
            if (CollUtils.isEmpty(courseVOS)) {
                courseVOS = queryTopNCourseOnMarketByFree(false, CourseRepository.TYPE);
            }
        }
        //只要三个就行
        return courseVOS.stream().limit(3).collect(Collectors.toList());
    }


    /**
     * 根据课程ID列表查询课程信息
     */
    private List<CourseVO> queryCoursesByIds(List<Long> courseIds) throws IOException {
        // 创建MultiGet请求
        MultiGetRequest request = new MultiGetRequest();
        for (Long courseId : courseIds) {
            request.add(new MultiGetRequest.Item(CourseRepository.INDEX_NAME, courseId.toString()));
        }

        // 执行批量查询
        MultiGetResponse response = restClient.multiGet(request, RequestOptions.DEFAULT);

        // 处理结果
        List<CourseVO> courses = new ArrayList<>(courseIds.size());
        Set<Long> teacherIds = new HashSet<>();

        for (MultiGetItemResponse itemResponse : response.getResponses()) {
            if (itemResponse.isFailed()) {
                log.warn("查询课程失败: {}", itemResponse.getFailure().getMessage());
                continue;
            }

            GetResponse getResponse = itemResponse.getResponse();
            if (getResponse.isExists()) {
                // 转换为CourseVO
                CourseVO vo = JsonUtils.toBean(getResponse.getSourceAsString(), CourseVO.class);
                courses.add(vo);
                // 收集教师ID
                if (vo.getTeacher() != null) {
                    teacherIds.add(Long.valueOf(vo.getTeacher()));
                }
            }
        }

        // 查询教师信息
        if (!teacherIds.isEmpty()) {
            List<UserDTO> teachers = userClient.queryUserByIds(new ArrayList<>(teacherIds));
            Map<String, String> teacherMap = teachers.stream()
                    .collect(Collectors.toMap(t -> t.getId().toString(), UserDTO::getName));

            // 设置教师名称
            for (CourseVO course : courses) {
                course.setTeacher(teacherMap.getOrDefault(course.getTeacher(), "未知"));
            }
        }

        return courses;
    }


    @Override
    public List<CourseVO> queryBestTopN() {
        // 1.获取当前用户
        return queryTopNCourseOnMarketByFree(false, CourseRepository.SOLD);
    }

    @Override
    public List<CourseVO> queryNewTopN() {
        return queryTopNCourseOnMarketByFree(false, PUBLISH_TIME);
    }

    @Override
    public List<CourseVO> queryFreeTopN() {
        return queryTopNCourseOnMarketByFree(true, CourseRepository.SOLD);
    }

    private List<CourseVO> queryTopNCourseOnMarketByFree(boolean isFree, String sortBy) {
        // 1.获取当前用户
        Long id = UserContext.getUser();
        // 2.查询课程
        List<CourseVO> courses = null;
        if (id == null) {
            // 3.未登录，直接查询报名人数最多的
            courses = queryTopNByCategoryIdLv2sAndFree(
                    null, isFree, sortBy, false, interestsProperties.getTopNumber());
        } else {
            // 4.已登录，根据兴趣爱好查询
            List<Long> categoryIds = interestsService.queryMyInterestsIds();
            if (CollUtils.isEmpty(categoryIds)) {
                // 4.1.没有兴趣爱好，直接查询报名人数最多的
                courses = queryTopNByCategoryIdLv2sAndFree(
                        null, isFree, sortBy, false, interestsProperties.getTopNumber());
            } else {
                // 4.2.有爱好.查询爱好课程中报名人数最多的
                courses = queryTopNByCategoryIdLv2sAndFree(
                        categoryIds, isFree, sortBy, false, interestsProperties.getTopNumber());
            }
        }
        return courses;
    }

    private List<CourseVO> queryTopNByCategoryIdLv2sAndFree(
            List<Long> categoryIds, Boolean isFree, String sortBy, boolean isASC, int n) {
        // 1.准备Request
        SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 1.1.是否免费
        if(isFree != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.FREE, isFree));
        }
        // 1.2.分类id
        if (categoryIds != null) {
            if (categoryIds.size() == 1) {
                queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV2, categoryIds.get(0)));
            } else {
                queryBuilder.filter(QueryBuilders.termsQuery(CourseRepository.CATEGORY_ID_LV2, categoryIds));
            }
        }
        if(isFree != null || categoryIds != null) {
            request.source().query(queryBuilder);
        }
        // 1.3.TopN
        request.source().size(n).sort(sortBy, isASC ? SortOrder.ASC : SortOrder.DESC);
        // 2.发送请求
        SearchResponse response = null;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        }
        // 3.解析
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        if (hits == null || hits.length == 0) {
            return CollUtils.emptyList();
        }
        List<CourseVO> courses = new ArrayList<>(hits.length);
        Set<Long> teacherIds = new HashSet<>(hits.length);
        for (SearchHit hit : hits) {
            // 3.1.数据转换
            CourseVO vo = JsonUtils.toBean(hit.getSourceAsString(), CourseVO.class);
            // 3.2.获取分类id
            teacherIds.add(Long.valueOf(vo.getTeacher()));
            // 3.3.保存
            courses.add(vo);
        }
        teacherIds.remove(0L);
        if (teacherIds.size() == 0) {
            return courses;
        }
        // 4.查询教师
        List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
        AssertUtils.isNotEmpty(teachers, SearchErrorInfo.TEACHER_NOT_EXISTS);
        Map<String, String> tMap = teachers.stream()
                .collect(Collectors.toMap(t -> t.getId().toString(), UserDTO::getName));
        for (CourseVO c : courses) {
            c.setTeacher(tMap.getOrDefault(c.getTeacher(), "匿名"));
        }
        return courses;
    }

    @Override
    public PageDTO<CourseVO> queryCoursesForPortal(CoursePageQuery query) {
        if(UserContext.getUser()!=null){
            saveSearchHistory(query.getKeyword());
        }
        // 1.搜索数据
        SearchResponse response = searchForResponse(query, CourseVO.EXCLUDE_FIELDS);
        // 2.解析响应
        PageDTO<Course> result = handleSearchResponse(response, query.getPageSize());
        // 3.处理VO
        List<Course> list = result.getList();
        if (CollUtils.isEmpty(list)) {
            return PageDTO.empty(result.getTotal(), result.getPages());
        }
        // 3.1.查询教师信息
        List<Long> teacherIds = list.stream().map(Course::getTeacher).collect(Collectors.toList());
        List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
        AssertUtils.isNotEmpty(teachers, SearchErrorInfo.TEACHER_NOT_EXISTS);
        Map<Long, String> teacherMap = teachers.stream()
                .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        // 3.2.转换VO
        List<CourseVO> vos = new ArrayList<>(list.size());
        for (Course c : list) {
            CourseVO vo = BeanUtils.toBean(c, CourseVO.class);
            vo.setTeacher(teacherMap.getOrDefault(c.getTeacher(), "未知"));
            vos.add(vo);
        }
        return new PageDTO<>(result.getTotal(), result.getPages(), vos);
    }

    @Override
    public List<Long> queryCoursesIdByName(String keyword) {
        // 1.创建Request
        SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
        // 2.构建DSL
        request.source()
                .query(QueryBuilders.matchPhraseQuery(CourseRepository.DEFAULT_QUERY_NAME, keyword))
                .fetchSource(new String[]{"id"}, null);
        // 3.查询
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        }
        // 4.解析
        SearchHits searchHits = response.getHits();
        // 4.1.获取hits
        SearchHit[] hits = searchHits.getHits();
        if (hits.length == 0) {
            return CollUtils.emptyList();
        }
        // 4.2.获取id
        return Arrays.stream(hits)
                .map(SearchHit::getId)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }


    private SearchResponse searchForResponse(CoursePageQuery query, String[] excludeFields) {
        // 1.创建Request
        SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
        // 2.构建DSL
        // 2.1.构建query
        buildBasicQuery(request, query);
        // 2.2.排序
        String sortBy = query.getSortBy();
        if (StringUtils.isNotBlank(sortBy)) {
            request.source().sort(sortBy, query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
        }
        // 2.3.分页
        request.source().from(query.from()).size(query.getPageSize());
        // 2.4.高亮
        request.source().highlighter(new HighlightBuilder().field(CourseRepository.DEFAULT_QUERY_NAME));
        // 2.5.source处理
        request.source().fetchSource(null, excludeFields);
        // 3.发送请求
        SearchResponse response = null;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(ErrorInfo.Msg.SERVER_INTER_ERROR, e);
        }
        return response;
    }

    private void buildBasicQuery(SearchRequest request, CoursePageQuery query) {
        // 1.准备bool查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 2.关键字搜索
        String keyword = query.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            queryBuilder.must(QueryBuilders.matchAllQuery());
        } else {
            queryBuilder.must(QueryBuilders.matchPhraseQuery(CourseRepository.DEFAULT_QUERY_NAME, keyword));
        }
        // 3.其它条件
        if (query.getCategoryIdLv1() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV1, query.getCategoryIdLv1()));
        }
        if (query.getCategoryIdLv2() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV2, query.getCategoryIdLv2()));
        }
        if (query.getCategoryIdLv3() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV3, query.getCategoryIdLv3()));
        }
        if (query.getFree() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.FREE, query.getFree()));
        }
        if (query.getType() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.TYPE, query.getType()));
        }
        LocalDateTime beginTime = query.getBeginTime();
        LocalDateTime endTime = query.getEndTime();
        if(beginTime != null || endTime != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(CourseRepository.UPDATE_TIME);
            if (beginTime != null) {
                rangeQuery.gte(beginTime);
            }
            if (endTime != null) {
                rangeQuery.lte(endTime);
            }
            queryBuilder.filter(rangeQuery);
        }
        // 4.写入request
        request.source().query(queryBuilder);
    }

    private PageDTO<Course> handleSearchResponse(SearchResponse response, int pageSize) {
        SearchHits searchHits = response.getHits();
        // 1.总条数
        long total = searchHits.getTotalHits().value;
        // 2.总页数
        long totalPages = (total + pageSize - 1) / pageSize;
        // 3.获取命中的数据
        SearchHit[] hits = searchHits.getHits();
        if (hits.length <= 0) {
            return new PageDTO<>(total, totalPages, CollUtils.emptyList());
        }
        // 4.遍历
        List<Course> list = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            // 5.获取某一条source
            String jsonSource = hit.getSourceAsString();
            // 6.反序列化
            Course course = JsonUtils.toBean(jsonSource, Course.class);
            // 7.处理高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (CollUtils.isNotEmpty(highlightFields)) {
                // 7.1.获取高亮结果
                HighlightField field = highlightFields.get(CourseRepository.DEFAULT_QUERY_NAME);
                Object[] fragments = field.getFragments();
                String value = StringUtils.join(fragments);
                // 7.2.覆盖非高亮结果
                course.setName(value);
            }
            list.add(course);
        }
        return new PageDTO<>(total, totalPages, list);
    }



    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    //关键字自动补全
    @Override
    public List<String> completeSuggest(String keyword) {
        // 1. 构建搜索建议请求
        CompletionSuggestionBuilder keywordSuggestion = new CompletionSuggestionBuilder("keyword")
                .prefix(keyword)
                .size(10);

        CompletionSuggestionBuilder pinyinSuggestion = new CompletionSuggestionBuilder("keywordPinyin")
                .prefix(keyword)
                .size(10);

        CompletionSuggestionBuilder sequenceSuggestion = new CompletionSuggestionBuilder("keywordSequence")
                .prefix(keyword)
                .size(10);

        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion("keyword_suggest", keywordSuggestion)
                .addSuggestion("pinyin_suggest", pinyinSuggestion)
                .addSuggestion("sequence_suggest", sequenceSuggestion);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withSuggestBuilder(suggestBuilder)
                .build();

        // 2. 执行搜索 - 针对低版本的修改
        org.springframework.data.elasticsearch.core.SearchHits<?> searchHits =
                elasticsearchRestTemplate.search(searchQuery, SuggestIndex.class);

        // 3. 获取建议结果
        Suggest suggest = searchHits.getSuggest();
        if (suggest == null) {
            return List.of();
        }

        // 合并三个建议来源的结果
        List<String> suggestions = suggest.getSuggestion("keyword_suggest").getEntries().stream()
                .flatMap(e -> e.getOptions().stream())
                .map(option -> {
                    // 通过反射获取hitEntity字段
                    try {
                        Field hitEntityField = option.getClass().getDeclaredField("hitEntity");
                        hitEntityField.setAccessible(true);
                        Object hitEntity = hitEntityField.get(option);

                        if (hitEntity instanceof SuggestIndex) {
                            return ((SuggestIndex) hitEntity).getTitle();
                        }
                    } catch (Exception e) {
                        log.warn("无法获取hitEntity字段", e);
                    }
                    return option.getText().toString(); // 回退方案
                })
                .collect(Collectors.toList());

        suggestions.addAll(suggest.getSuggestion("pinyin_suggest").getEntries().stream()
                .flatMap(e -> e.getOptions().stream())
                .map(option -> {
                    // 通过反射获取hitEntity字段
                    try {
                        Field hitEntityField = option.getClass().getDeclaredField("hitEntity");
                        hitEntityField.setAccessible(true);
                        Object hitEntity = hitEntityField.get(option);

                        if (hitEntity instanceof SuggestIndex) {
                            return ((SuggestIndex) hitEntity).getTitle();
                        }
                    } catch (Exception e) {
                        log.warn("无法获取hitEntity字段", e);
                    }
                    return option.getText().toString(); // 回退方案
                })
                .collect(Collectors.toList()));

        suggestions.addAll(suggest.getSuggestion("sequence_suggest").getEntries().stream()
                .flatMap(e -> e.getOptions().stream())
                .map(option -> {
                    // 通过反射获取hitEntity字段
                    try {
                        Field hitEntityField = option.getClass().getDeclaredField("hitEntity");
                        hitEntityField.setAccessible(true);
                        Object hitEntity = hitEntityField.get(option);

                        if (hitEntity instanceof SuggestIndex) {
                            return ((SuggestIndex) hitEntity).getTitle();
                        }
                    } catch (Exception e) {
                        log.warn("无法获取hitEntity字段", e);
                    }
                    return option.getText().toString(); // 回退方案
                })
                .collect(Collectors.toList()));

        // 去重并返回
        return suggestions.stream().distinct().collect(Collectors.toList());
    }


    public void saveSearchHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        String key = getHistoryKey();
        keyword = keyword.trim();

        // 使用 ZSet 存储搜索历史，分数为当前时间戳，确保最新搜索在前面
        redisTemplate.opsForZSet().add(key, keyword, System.currentTimeMillis());

        // 限制历史记录数量，移除最旧的记录
        Long size = redisTemplate.opsForZSet().zCard(key);
        if (size != null && size > MAX_HISTORY_SIZE) {
            // 获取需要移除的最旧元素
            Set<String> oldValues = redisTemplate.opsForZSet().range(key, 0, size - MAX_HISTORY_SIZE - 1);
            if (oldValues != null && !oldValues.isEmpty()) {
                redisTemplate.opsForZSet().remove(key, oldValues.toArray());
            }
        }
    }

    @Override
    public List<String> getSearchHistory() {
        if(UserContext.getUser()==null){
            return new ArrayList<>();
        }
        String key = getHistoryKey();
        // 按分数（时间戳）倒序获取，最新的搜索在前面
        Set<String> historySet = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        return historySet != null ? new ArrayList<>(historySet) : new ArrayList<>();
    }

    @Override
    public void deleteSearchHistory(String keyword) {
        if(UserContext.getUser()==null){
            return;
        }
        if (keyword == null || keyword.trim().isEmpty() || UserContext.getUser() == null) {
            return;
        }
        String key = getHistoryKey();
        keyword = keyword.trim();
        // 从ZSet中移除指定关键字的搜索历史
        redisTemplate.opsForZSet().remove(key, keyword);
    }
    @Override
    public void clearSearchHistory() {
        if(UserContext.getUser()==null){
            return;
        }
        String key = getHistoryKey();
        redisTemplate.delete(key);
    }

    // 获取当前用户的搜索历史键
    private String getHistoryKey() {
        Long userId = UserContext.getUser(); // 获取当前用户ID
        return SEARCH_HISTORY_KEY_PREFIX + userId;
    }

}
