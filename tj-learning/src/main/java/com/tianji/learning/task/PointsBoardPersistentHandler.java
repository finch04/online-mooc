package com.tianji.learning.task;


import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.tianji.learning.service.IPointsBoardService;
import com.tianji.learning.utils.TableInfoContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.tianji.learning.constants.LearningConstants.POINTS_BOARD_TABLE_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsBoardPersistentHandler {

    private final IPointsBoardSeasonService seasonService;

    private final IPointsBoardService pointsBoardService;

    private final StringRedisTemplate redisTemplate;



    /**
     * 创建上赛季（上个月）的榜单表
     */
//    @Scheduled(cron = "0 0 3 1 * ?")// 每月1号，凌晨3点执行
//    @Scheduled(cron = "0 53 17 27 10 ?")// 测试
    @XxlJob("createTableJob")
    public void createPointsBoardTableOfLastSeason() {
        log.debug("创建上个月的榜单表任务执行了");
        //获取上个月的时间点
        LocalDate time =LocalDate.now().minusMonths(1);

        //查询赛季表获取id 条件beginTime<=time and endTime>=time
        PointsBoardSeason one = seasonService.lambdaQuery()
                .le(PointsBoardSeason::getBeginTime, time)
                .ge(PointsBoardSeason::getEndTime, time)
                .one();
        log.debug("上赛季信息：{}",one);
        if(one == null){
            return ;
        }

        //创建上个赛季的榜单表
        seasonService.createPointsBoardLatestTable(one.getId());

    }

    //持久化表单
    @XxlJob("savePointsBoard2DB")
    public void savePointsBoard2DB(){
        //获取上个月，当前时间点
        LocalDate time =LocalDate.now().minusMonths(1);

        //查询赛季表获取id 条件beginTime<=time and endTime>=time
        PointsBoardSeason one = seasonService.lambdaQuery()
                .le(PointsBoardSeason::getBeginTime, time)
                .ge(PointsBoardSeason::getEndTime, time)
                .one();
        log.debug("上赛季信息：{}",one);
        if(one == null){
            return ;
        }
        //计算动态表名，并存入threadlocal
        String tableName = POINTS_BOARD_TABLE_PREFIX+one.getId();
        log.debug("动态表名为：{}",tableName);
        TableInfoContext.setInfo(tableName);

        //分页获取redis上赛季的积分排行榜
        String format =time.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX+format;

        int shardIndex = XxlJobHelper.getShardIndex();//当前分片的索引，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//总分片数


        //定义合适的从redis取数据的数量
        int pageNo =shardIndex+1;
        int pageSize =1000;
        while(true){
            log.debug("处理第{}页数据",pageNo);
            List<PointsBoard> pointsBoardList = pointsBoardService.queryCurrentBoard(key, pageNo, pageSize);
            if(CollUtils.isEmpty(pointsBoardList)){
                break;//遍历完则跳出循环
            }
            pageNo+=shardTotal;
            //持久化到db相应的赛季表中 批量新增
            for (PointsBoard board : pointsBoardList) {
                board.setId(Long.valueOf(board.getRank()));
                board.setRank(null);
            }
            pointsBoardService.saveBatch(pointsBoardList);
            //todo 删除本页redis数据 以确保发生有分片先读取完数据后直接删除了redis，导致其他分片无法继续读取
        }
        //清空threadlocal的数据
        TableInfoContext.remove();
    }


    @XxlJob("clearPointsBoardFromRedis")
    public void clearPointsBoardFromRedis(){
        // 1.获取上月时间
        LocalDateTime time = LocalDateTime.now().minusMonths(1);
        // 2.计算key
        String format =time.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX+format;
        // 3.删除
        redisTemplate.unlink(key);
    }
}
