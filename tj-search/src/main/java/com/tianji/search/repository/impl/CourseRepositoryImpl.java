package com.tianji.search.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.DeleteOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation;
import co.elastic.clients.json.JsonData;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.CollUtils;
import com.tianji.search.domain.po.Course;
import com.tianji.search.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tianji.search.constants.SearchErrorInfo.*;

@Slf4j
@Component
public class CourseRepositoryImpl implements CourseRepository {

    private final ElasticsearchClient esClient;

    public CourseRepositoryImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public void save(Course course) {
        try {
            IndexRequest<Course> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(course.getId().toString())
                    .document(course)
            );
            esClient.index(request);
        } catch (Exception e) {
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }

    @Override
    public void deleteById(Long courseId) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(INDEX_NAME)
                    .id(courseId.toString())
            );
            esClient.delete(request);
        } catch (Exception e) {
            throw new CommonException(DELETE_COURSE_ERROR, e);
        }
    }

    @Override
    public Optional<Course> findById(Long courseId) {
        try {
            GetRequest request = GetRequest.of(g -> g
                    .index(INDEX_NAME)
                    .id(courseId.toString())
            );
            GetResponse<Course> response = esClient.get(request, Course.class);
            return response.found() ? Optional.of(response.source()) : Optional.empty();
        } catch (IOException e) {
            throw new CommonException(QUERY_COURSE_ERROR, e);
        }
    }

    @Override
    public void updateById(Long courseId, Object... sources) {
        try {
            Map<String, Object> doc = new HashMap<>();
            for (int i = 0; i < sources.length; i += 2) {
                doc.put(sources[i].toString(), sources[i + 1]);
            }

            UpdateRequest<Course, Map<String, Object>> request = UpdateRequest.of(u -> u
                    .index(INDEX_NAME)
                    .id(courseId.toString())
                    .doc(doc)
            );
            esClient.update(request, Course.class);
        } catch (Exception e) {
            throw new CommonException(UPDATE_COURSE_STATUS_ERROR, e);
        }
    }

    @Override
    public void increment(Long courseId, String field, int amount) {
        try {
            Script script = Script.of(s -> s
                    .inline(i -> i
                            .lang("painless")
                            .source("ctx._source." + field + " += params.count")
                            .params("count", JsonData.of(amount))
                    )
            );

            UpdateRequest<Course, Object> request = UpdateRequest.of(u -> u
                    .index(INDEX_NAME)
                    .id(courseId.toString())
                    .script(script)
            );
            esClient.update(request, Course.class);
        } catch (Exception e) {
            throw new CommonException(UPDATE_COURSE_STATUS_ERROR, e);
        }
    }

    @Override
    public void incrementSold(List<Long> courseIds, int amount) {
        if (CollUtils.isEmpty(courseIds)) {
            log.warn("批量更新销量，课程ID列表为空");
            return;
        }

        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

            // 构建更新脚本（明确指定脚本类型和语言）
            Script script = Script.of(s -> s
                    .inline(i -> i
                            .lang("painless")
                            .source("ctx._source.sold += params.count")
                            .params("count", JsonData.of(amount))
                    )
            );

            for (Long courseId : courseIds) {
                if (courseId == null) {
                    log.warn("跳过空的课程ID");
                    continue;
                }

                // 构建更新操作（正确嵌套 index 和 script）
                bulkBuilder.operations(op -> op
                        .update(u -> u
                                .index(INDEX_NAME)  // index 属于 update 操作的参数
                                .id(courseId.toString())
                                .action(a -> a.script(script))// script 属于 update 操作的参数
                        )
                );
            }

            // 执行批量操作
            BulkResponse response = esClient.bulk(bulkBuilder.build());
            if (response.errors()) {
                handleBulkErrors(response);
                throw new CommonException(UPDATE_COURSE_STATUS_ERROR);
            }
        } catch (Exception e) {
            log.error("批量更新销量失败", e);
            throw new CommonException(UPDATE_COURSE_STATUS_ERROR, e);
        }
    }

    @Override
    public void saveAll(List<Course> list) {
        if (CollUtils.isEmpty(list)) {
            log.warn("批量保存课程，课程列表为空");
            return;
        }

        // 过滤掉id为null的无效课程
        List<Course> validCourses = list.stream()
                .filter(course -> course.getId() != null) // 关键：筛选有效ID
                .collect(Collectors.toList());

        if (CollUtils.isEmpty(validCourses)) {
            log.warn("批量保存课程，所有课程ID均为null，无法执行保存");
            return; // 没有有效数据，直接返回
        }

        try {
            // 使用过滤后的有效课程构建操作
            List<BulkOperation> operations = validCourses.stream()
                    .map(course -> BulkOperation.of(b -> b
                            .index(IndexOperation.of(i -> i
                                    .index(INDEX_NAME)
                                    .id(course.getId().toString()) // 此时ID一定不为null
                                    .document(course)
                            ))
                    ))
                    .collect(Collectors.toList());

            BulkRequest request = BulkRequest.of(b -> b
                    .operations(operations)
            );
            esClient.bulk(request);
        } catch (IOException e) {
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }

    @Override
    public void deleteByIds(List<Long> courseIds) {
        if (CollUtils.isEmpty(courseIds)) {
            log.warn("批量删除课程，课程ID列表为空");
            return;
        }

        try {
            List<BulkOperation> operations = courseIds.stream()
                    .map(id -> BulkOperation.of(b -> b
                            .delete(DeleteOperation.of(d -> d
                                    .index(INDEX_NAME)
                                    .id(id.toString())
                            ))
                    ))
                    .collect(Collectors.toList());

            BulkRequest request = BulkRequest.of(b -> b
                    .operations(operations)
            );
            esClient.bulk(request);
        } catch (IOException e) {
            throw new CommonException(DELETE_COURSE_ERROR, e);
        }
    }

    /**
     * 处理批量操作错误
     */
    private void handleBulkErrors(BulkResponse response) {
        response.items().forEach(item -> {
            if (item.error() != null) {
                log.error("批量操作失败，ID: {}, 错误原因: {}",
                        item.id(), item.error().reason());
            }
        });
    }
}