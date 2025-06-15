package com.tianji.search;/**
 * @author fsq
 * @date 2025/6/15 10:26
 */

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.search.domain.po.SuggestIndex;
import com.tianji.search.repository.SuggestIndexRepository;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: fsq
 * @Date: 2025/6/15 10:26
 * @Version: 1.0
 */
@SpringBootTest
public class SuggestTest {

    @Autowired
    private SuggestIndexRepository suggestIndexRepository;
    @Autowired
    private CourseClient courseClient;


    //导入之前课程提词器
    @Test
    public void test() {
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list.add(5L);
        list.add(6L);
        list.add(7L);
        list.add(8L);
        list.add(10L);
        list.add(1552558707325374467L);
        list.add(1589888774267072513L);
        list.add(1549025085494521857L);

        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(list);
        for (CourseSimpleInfoDTO dto : simpleInfoList) {
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


//    private static final String SUGGEST_INDEX = "suggestinfo";
//
//    @Autowired
//    private RestHighLevelClient restClient;
//
//
//    @Test
//    public void SuggestTest() {
//        List<String> strings = completeSuggest("kt");
//        System.out.println(strings);
//    }
//
//    //关键字自动补全
//    public List<String> completeSuggest(String keyword) {
//        try {
//            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(restClient, SearchAction.INSTANCE);
//            searchRequestBuilder.setIndices(SUGGEST_INDEX);
//            //设置建议请求体参数
//            searchRequestBuilder.suggest(new SuggestBuilder()
//                    .addSuggestion("suggestKeyword",new CompletionSuggestionBuilder("keyword").size(10).skipDuplicates(true))
//                    .addSuggestion("suggestKeywordPinyin",new CompletionSuggestionBuilder("keywordPinyin").size(10).skipDuplicates(true))
//                    .addSuggestion("suggestKeywordSquenece",new CompletionSuggestionBuilder("keywordSequence").size(10).skipDuplicates(true))
//            );
//            //执行检索
//            SearchRequest searchRequest = searchRequestBuilder.request();
//            System.out.println("提词DSL：");
//            System.out.println(searchRequest.toString());
//            SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
//            //解析ES响应结果
//            //准备单例集合（能去重重复建议词）
//            Set<String> titleSet = new HashSet<>();
////            titleSet.addAll(this.parseSuggestResult(searchResponse, "suggestKeyword"));
////            titleSet.addAll(this.parseSuggestResult(searchResponse, "suggestKeywordPinyin"));
////            titleSet.addAll(this.parseSuggestResult(searchResponse, "suggestKeywordSquenece"));
//            //判断命中提示词数量<10
//            if(titleSet.size() < 10){
//                //通过普通检索 检索提词索引库 利用匹配查询返回结果
//                SearchResponse response =
//                        restClient.search(new SearchRequest().indices(SUGGEST_INDEX),  RequestOptions.DEFAULT);
//                SearchHits hits = response.getHits();
//                if(CollectionUtil.isNotEmpty(hits)){
//
//                    for (SearchHit hit : hits) {
//                        String suggestIndex = hit.getSourceAsString();
////                        titleSet.add(suggestIndex.getTitle());
//                        //如果添加后大于等于10
//                        if(titleSet.size() > 10){
//                            break;
//                        }
//                    }
//                }
//            }
//            return new ArrayList<>(titleSet).subList(0, titleSet.size()>10?10:titleSet.size());
////            return new ArrayList<>(titleSet);
//        } catch (Exception e) {
//            System.out.println("失败");
//        }
//        return null;
//    }

}
