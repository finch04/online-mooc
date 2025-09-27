package com.tianji.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import com.tianji.common.utils.RandomUtils;
import com.tianji.search.domain.po.Course;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@Slf4j
class CourseRepositoryTest {

    @Autowired
    private CourseRepository repository;

    @Test
    void save() {
        LocalDateTime time = LocalDateTime.now().minusDays(RandomUtils.randomInt(20));
        Course course = new Course();
        course.setId(1204101L);
        course.setCategoryIdLv1(1001L);
        course.setCategoryIdLv2(2000L + RandomUtils.randomInt(1,9));
        course.setCategoryIdLv3(3007L);
        course.setCoverUrl("default-cover-url.jpg");
        course.setFree(RandomUtils.randomBoolean());
        course.setName("Java实战课");
        course.setPublishTime(time);
        course.setScore(30 + RandomUtils.randomInt(20));
        course.setSections(10 + RandomUtils.randomInt(10));
        course.setSold(200 + RandomUtils.randomInt(1000));
        course.setTeacher(1548889371405492225L);
        course.setType(2);
        repository.save(course);
    }

    @Test
    void deleteById() {
        repository.deleteById(1204101L);
    }

    @Test
    void findById() {
        Optional<Course> op = repository.findById(1204101L);
        op.ifPresent(System.out::println);
    }

    @Test
    void updateById() {
    }

    @Test
    void increment() {
        repository.increment(1204101L, "sold", -1);
    }

    @Test
    void test() throws IOException {
        checkAndFixMapping();
    }

    @Resource
    private ElasticsearchClient esClient;
    // 检查当前映射
    public void checkAndFixMapping() throws IOException {
        GetMappingResponse mapping = esClient.indices().getMapping(g -> g
                .index(CourseRepository.INDEX_NAME));

        log.info("当前映射: {}", mapping);

        // 如果publishTime是text类型，需要重建索引或更新映射
        updateMappingToDateType();
    }

    // 更新映射为日期类型
    private void updateMappingToDateType() throws IOException {
        PutMappingResponse response = esClient.indices().putMapping(p -> p
                .index(CourseRepository.INDEX_NAME)
                .properties("publishTime", prop -> prop
                        .date(d -> d.format("yyyy-MM-dd HH:mm:ss||epoch_millis"))
                ));

        log.info("映射更新结果: {}", response);
    }

}