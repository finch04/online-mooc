package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardItemVO;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.mapper.PointsBoardMapper;
import com.tianji.learning.service.IPointsBoardService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.utils.TableInfoContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.learning.constants.LearningConstants.POINTS_BOARD_TABLE_PREFIX;

/**
 * <p>
 * 学霸天梯榜 服务实现类
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
@Service
@RequiredArgsConstructor
public class PointsBoardServiceImpl extends ServiceImpl<PointsBoardMapper, PointsBoard> implements IPointsBoardService {

    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;

    //查询学霸排行榜
    @Override
    public PointsBoardVO queryPointsBoardList(PointsBoardQuery query) {
        /**
        //获取当前登录用户id
        Long userId = UserContext.getUser();
        //判断是查当前赛季还是历史赛季 query.season 赛季id 为null 或者 0 则代表查询当前赛季
        boolean isCurrent=query.getSeason()==null||query.getSeason()==0;
        LocalDateTime now =LocalDateTime.now();
        String format =now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX+format;
        Long season=query.getSeason();//历史赛季id
        //查询我的排名和积分 根据query.season 判断是查redis还是db
        PointsBoard board=isCurrent?queryMyCurrentBoard(key):queryMyHistoryBoard(season);
        //分页查询赛季列表 根据query.season 判断是查redis还是db
        List<PointsBoard> list=isCurrent?queryCurrentBoard(key,query.getPageNo(),query.getPageSize()):queryHistoryBoard(query);
        //远程调用用户服务，通过用户id集合 转map 获得用户信息
        Set<Long> uids = list.stream().map(PointsBoard::getUserId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(uids);
        if(CollUtils.isEmpty(userDTOS)){
            throw new BizIllegalException("用户不存在");
        }
        Map<Long, String> userDtoMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c.getName()));

        //封装vo返回
        PointsBoardVO vo =new PointsBoardVO();
        vo.setPoints(board.getPoints());
        vo.setRank(board.getRank());

        List<PointsBoardItemVO> voList =new ArrayList<>();
        for (PointsBoard pointsBoard : list) {
            PointsBoardItemVO itemVo =new PointsBoardItemVO();
            itemVo.setName(userDtoMap.get(pointsBoard.getUserId()));
            itemVo.setPoints(pointsBoard.getPoints());
            itemVo.setRank(pointsBoard.getRank());

            voList.add(itemVo);
        }

        vo.setBoardList(voList);
        return vo;
         **/
        // 1.判断是否是查询当前赛季
        Long season = query.getSeason();
        boolean isCurrent = season == null || season == 0;
        // 2.获取Redis的Key
        LocalDateTime now = LocalDateTime.now();
        String format =now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX+format;
        // 2.查询我的积分和排名
        PointsBoard myBoard = isCurrent ?
                queryMyCurrentBoard(key) : // 查询当前榜单（Redis）
                queryMyHistoryBoard(season); // 查询历史榜单（MySQL）
        // 3.查询榜单列表
        List<PointsBoard> list = isCurrent ?
                queryCurrentBoard(key, query.getPageNo(), query.getPageSize()) :
                queryHistoryBoard(query);
        // 4.封装VO
        PointsBoardVO vo = new PointsBoardVO();
        // 4.1.处理我的信息
        if (myBoard != null) {
            vo.setPoints(myBoard.getPoints());
            vo.setRank(myBoard.getRank());
        }
        if (CollUtils.isEmpty(list)) {
            return vo;
        }
        // 4.2.查询用户信息
        Set<Long> uIds = list.stream().map(PointsBoard::getUserId).collect(Collectors.toSet());
        List<UserDTO> users = userClient.queryUserByIds(uIds);
        Map<Long, String> userMap = new HashMap<>(uIds.size());
        if(CollUtils.isNotEmpty(users)) {
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        }
        // 4.3.转换VO
        List<PointsBoardItemVO> items = new ArrayList<>(list.size());
        for (PointsBoard p : list) {
            PointsBoardItemVO v = new PointsBoardItemVO();
            v.setPoints(p.getPoints());
            v.setRank(p.getRank());
            v.setName(userMap.get(p.getUserId()));
            items.add(v);
        }
        vo.setBoardList(items);
        return vo;
    }

    //查询当前赛季排名 从redis查
    public List<PointsBoard> queryCurrentBoard(String key, Integer pageNo, Integer pageSize) {
        //计算start和stop 计算分页值
        int start = (pageNo - 1) * pageSize;
        int stop = start + pageSize - 1;
        //利用zrevrange 分数查询 倒序展示
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, stop);
        if(CollUtils.isEmpty(typedTuples)){
            return CollUtils.emptyList();
        }
        //封装结果返回
        int rank=start+1;
        List<PointsBoard> list =new ArrayList<>();

        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            String value = typedTuple.getValue();//用户id
            Double score = typedTuple.getScore();//总积分值
            if(StringUtils.isBlank(value)||score==null){
                continue;
            }

            PointsBoard board=new PointsBoard();
            board.setRank(rank++);
            board.setPoints(score.intValue());
            board.setUserId(Long.valueOf(value));
            list.add(board);
        }


        return list;
    }

    //查询历史赛季排名 从db查
    private List<PointsBoard> queryHistoryBoard(PointsBoardQuery query) {
        // 1.计算表名
        TableInfoContext.setInfo(POINTS_BOARD_TABLE_PREFIX + query.getSeason());
        // 2.查询数据
        Page<PointsBoard> page = page(query.toMpPage());
        // 3.数据处理
        List<PointsBoard> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return CollUtils.emptyList();
        }
        records.forEach(b -> b.setRank(b.getId().intValue()));
        return records;
    }

    //查询历史赛季积分和排名
    private PointsBoard queryMyHistoryBoard(Long season) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.计算表名
        TableInfoContext.setInfo(POINTS_BOARD_TABLE_PREFIX + season);
        // 3.查询数据
        Optional<PointsBoard> opt = null;

        try {
            opt = lambdaQuery().eq(PointsBoard::getUserId, userId).oneOpt();
        } catch (Exception e) {
            throw new BizIllegalException("当前赛季数据已经丢失，不予显示！");
        }


        if (opt.isEmpty()) {
            return null;
        }
        // 4.转换数据
        PointsBoard pointsBoard = opt.get();
        pointsBoard.setRank(pointsBoard.getId().intValue());
        return pointsBoard;
    }

    //查询当前赛季积分和排名
    private PointsBoard queryMyCurrentBoard(String key) {
        Long userId =UserContext.getUser();
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());

        PointsBoard board=new PointsBoard();
        board.setRank(rank==null?0:rank.intValue()+1);
        board.setPoints(score==null?0: score.intValue());
        return board;
    }
}
