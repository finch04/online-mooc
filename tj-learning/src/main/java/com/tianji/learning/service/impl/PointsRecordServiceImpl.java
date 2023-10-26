package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mapper.PointsRecordMapper;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学习积分记录，每个月底清零 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
@Service
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements IPointsRecordService {

    //增加积分
    @Override
    public void addPointsRecord(SignInMessage msg, PointsRecordType type) {
        //校验消息
        if(msg.getUserId()==null || msg.getPoints()==null){
            return;
        }
        //判断积分是否有上限
        int maxPoints = type.getMaxPoints();
        int realPoints=msg.getPoints();//实际积分
        if(maxPoints>0){
            //如果有上限 查询该用户该积分类型，今日已得积分 points_record 条件 userId type 今天 sum(points)
            LocalDateTime now =LocalDateTime.now();
            LocalDateTime dayStartTime = DateUtils.getDayStartTime(now);
            LocalDateTime dayEndTime = DateUtils.getDayEndTime(now);
            QueryWrapper<PointsRecord> wrapper=new QueryWrapper<>();
            wrapper.select("sum(points) as totalPoints");
            wrapper.eq("user_id",msg.getUserId());
            wrapper.eq("type",type);
            wrapper.between("create_time",dayStartTime,dayEndTime);

            Map<String, Object> map = this.getMap(wrapper);

            int currentPoints=0;//该用户此类型今日已得积分
            if(map!=null){
                BigDecimal  totalPoints = (BigDecimal) map.get("totalPoints");
                currentPoints=totalPoints.intValue();
            }
            //判断已得积分是否超过上限
            if(currentPoints>=maxPoints){
                //说明已得积分达到上限
                return;
            }
            if(realPoints+currentPoints>maxPoints){
                realPoints=maxPoints-currentPoints;
            }


        }

        //保存积分
        PointsRecord record =new PointsRecord();
        record.setUserId(msg.getUserId());
        record.setPoints(realPoints);
        record.setType(type);
        this.save(record);

    }

    //查询我的今日积分情况
    @Override
    public List<PointsStatisticsVO> queryMyTodayPoints() {
        //获取user
        Long userId = UserContext.getUser();
        LocalDateTime now =LocalDateTime.now();
        LocalDateTime dayStartTime = DateUtils.getDayStartTime(now);
        LocalDateTime dayEndTime = DateUtils.getDayEndTime(now);

        //此处用points（积分值）暂存今日已经获取的积分
       QueryWrapper<PointsRecord> wrapper =new QueryWrapper<>();
       wrapper.select("type","sum(points) as points");
       wrapper.eq("user_id",userId);
       wrapper.between("create_time",dayStartTime,dayEndTime);
       wrapper.groupBy("type");


        List<PointsRecord> list = this.list(wrapper);
        if(CollUtils.isEmpty(list)){
            return CollUtils.emptyList();
        }

        //封装vo返回
        List<PointsStatisticsVO> voList=new ArrayList<>();
        for (PointsRecord r : list) {
            PointsStatisticsVO vo =new PointsStatisticsVO();
            vo.setMaxPoints(r.getType().getMaxPoints());
            vo.setType(r.getType().getDesc()); //积分类型的中文
            vo.setPoints(r.getPoints());

            voList.add(vo);
        }


        return voList;
    }


}
