package com.tianji.search.repository.impl;

import com.tianji.search.domain.po.OrderDetail;
import com.tianji.search.repository.OrderDetailRepository;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tianji.search.constants.SearchErrorInfo.*;

@Slf4j
@Component
public class OrderDetailRepositoryImpl implements OrderDetailRepository {

    private final RestHighLevelClient restHighLevelClient;

    public OrderDetailRepositoryImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public void save(OrderDetail orderDetail) {
        IndexRequest request = new IndexRequest(INDEX_NAME)
                .id(orderDetail.getId().toString())
                .source(JsonUtils.toJsonStr(orderDetail), XContentType.JSON);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new CommonException(SAVE_ORDER_DETAIL_ERROR, e);
        }
    }

    @Override
    public void deleteById(Long orderDetailId) {
        try {
            restHighLevelClient.delete(new DeleteRequest(INDEX_NAME, orderDetailId.toString()), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new CommonException(SAVE_ORDER_DETAIL_ERROR, e);
        }
    }

    @Override
    public Optional<OrderDetail> findById(Long orderDetailId) {
        GetResponse response = null;
        try {
            response = restHighLevelClient.get(new GetRequest(INDEX_NAME, orderDetailId.toString()), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(QUERY_ORDER_DETAIL_ERROR, e);
        }
        String source = response.getSourceAsString();
        if (StringUtils.isBlank(source)) {
            return Optional.empty();
        }
        return Optional.of(JsonUtils.toBean(source, OrderDetail.class));
    }

    @Override
    public void updateById(Long orderDetailId, Object... sources) {
        UpdateRequest request = new UpdateRequest(INDEX_NAME, orderDetailId.toString());
        request.doc(sources);
        try {
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new CommonException(UPDATE_ORDER_DETAIL_STATUS_ERROR, e);
        }
    }


    @Override
    public void saveAll(List<OrderDetail> list) {
        BulkRequest request = new BulkRequest(INDEX_NAME);
        for (OrderDetail orderDetail : list) {
            request.add(new IndexRequest(INDEX_NAME)
                    .id(orderDetail.getId().toString())
                    .source(JsonUtils.toJsonStr(orderDetail), XContentType.JSON));
        }
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
                if (itemResponse.status().compareTo(RestStatus.BAD_REQUEST) >= 0) {
                    log.error("批处理失败，id:{}, 原因:{}", itemResponse.getId(), itemResponse.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new CommonException(SAVE_ORDER_DETAIL_ERROR, e);
        }
    }

    @Override
    public void deleteByIds(List<Long> orderDetailIds) {
        BulkRequest request = new BulkRequest(INDEX_NAME);
        for (Long orderDetailId : orderDetailIds) {
            request.add(new DeleteRequest(INDEX_NAME, orderDetailId.toString()));
        }
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
                if (itemResponse.status().compareTo(RestStatus.BAD_REQUEST) >= 0) {
                    log.error("批处理失败，id:{}, 原因:{}", itemResponse.getId(), itemResponse.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new CommonException(SAVE_ORDER_DETAIL_ERROR, e);
        }
    }
}