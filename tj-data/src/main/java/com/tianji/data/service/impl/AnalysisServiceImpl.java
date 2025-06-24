package com.tianji.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.mapper.CourseProfileMapper;
import com.tianji.data.mapper.UserProfileMapper;
import com.tianji.data.model.po.*;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.query.TimeRange;
import com.tianji.data.model.vo.*;
import com.tianji.data.service.*;
import com.tianji.data.utils.TimeHandlerUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements IAnalysisService {

    private final ICourseConversionDpvService courseConversionDpvService;
    private final ICourseDetailGenderDuvService courseDetailGenderDuvService;
    private final ICourseDetailProvinceDuvService courseDetailProvinceDuvService;
    private final UserClient userClient;
    private final CourseClient courseClient;
    private final IUserProfileService userProfileService;
    private final ICourseProfileService courseProfileService;

    @Override
    public FunnelPlotChartsVO courseConversionDpv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<CourseConversionDpv> list = courseConversionDpvService.lambdaQuery()
                .gt(CourseConversionDpv::getReportTime, timeRange.getBegin())
                .le(CourseConversionDpv::getReportTime, timeRange.getEnd())
                .list();

        // 计算总浏览次数和总下单次数
        long totalBrowseDpv = list.stream().mapToLong(CourseConversionDpv::getDoBrowseDpv).sum();
        long totalOrderDpv = list.stream().mapToLong(CourseConversionDpv::getDoOrderDpv).sum();

        // 封装漏斗图数据
        List<String> labels = new ArrayList<>();
        List<FunnelPlotChartsIndexVO> values = new ArrayList<>();

        labels.add("课程浏览");
        values.add(FunnelPlotChartsIndexVO.builder().name("课程浏览").value(totalBrowseDpv).build());

        labels.add("课程下单");
        values.add(FunnelPlotChartsIndexVO.builder().name("课程下单").value(totalOrderDpv).build());

        return FunnelPlotChartsVO.builder()
                .label(labels)
                .value(values)
                .build();
    }

    @Override
    public EchartsVO courseDetailGenderDuv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<CourseDetailGenderDuv> list = courseDetailGenderDuvService.lambdaQuery()
                .gt(CourseDetailGenderDuv::getReportTime, timeRange.getBegin())
                .le(CourseDetailGenderDuv::getReportTime, timeRange.getEnd())
                .list();

        // 计算男性和女性的总访问数
        long totalManDpv = list.stream().mapToLong(CourseDetailGenderDuv::getManDpv).sum();
        long totalWomanDpv = list.stream().mapToLong(CourseDetailGenderDuv::getWomanDpv).sum();

        // 创建 EchartsVO 对象
        EchartsVO echartsVO = new EchartsVO();

        // 创建饼图系列数据
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("name", "男", "value", totalManDpv));
        data.add(Map.of("name", "女", "value", totalWomanDpv));

        SerierVO serierVO = SerierVO.builder()
                .name("课程详情访问数")
                .type(SerierVO.TYPE_PIE)
                .data(data)
                .build();

        echartsVO.setSeries(Collections.singletonList(serierVO));
        return echartsVO;
    }

    @Override
    public EchartsVO courseDetailProvinceDuv(FlowQuery query) {
        TimeRange timeRange = getTimeRange(query);
        List<CourseDetailProvinceDuv> list = courseDetailProvinceDuvService.lambdaQuery()
                .gt(CourseDetailProvinceDuv::getReportTime, timeRange.getBegin())
                .le(CourseDetailProvinceDuv::getReportTime, timeRange.getEnd())
                .list();

        // 按省份分组并计算总访问数，取前10
        List<Map.Entry<String, Long>> top10Provinces = list.stream()
                .collect(Collectors.groupingBy(
                        CourseDetailProvinceDuv::getProvinceName,
                        Collectors.summingLong(CourseDetailProvinceDuv::getDuv)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // 创建 EchartsVO 对象
        EchartsVO echartsVO = new EchartsVO();

        // 创建 x 轴数据
        List<AxisVO> xAxis = new ArrayList<>();
        List<String> provinceNames = top10Provinces.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        xAxis.add(AxisVO.builder()
                .type(AxisVO.TYPE_CATEGORY)
                .data(provinceNames)
                .build());

        // 创建 y 轴数据
        List<AxisVO> yAxis = new ArrayList<>();
        yAxis.add(AxisVO.builder()
                .type(AxisVO.TYPE_VALUE)
                .build());

        // 创建柱状图系列数据
        List<Long> duvValues = top10Provinces.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        SerierVO serierVO = SerierVO.builder()
                .name("课程详情访问数")
                .type(SerierVO.TYPE_BAR)
                .data(duvValues)
                .build();

        echartsVO.setXAxis(xAxis);
        echartsVO.setYAxis(yAxis);
        echartsVO.setSeries(Collections.singletonList(serierVO));
        return echartsVO;
    }

    @Override
    @Transactional
    public LogAnalysisResult analyzeLogs(List<BusinessLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return new LogAnalysisResult();
        }

        LogAnalysisResult result = new LogAnalysisResult();
        Map<String, UserProfile> userProfileMap = new HashMap<>();
        Map<String, CourseProfile> courseProfileMap = new HashMap<>();

        for (BusinessLog log : logs) {
            // 检查关键字段是否为空，任一为空则跳过
            if (log.getUserId() == null ||
                    log.getUserName() == null ||
                    log.getSex() == null ||
                    log.getProvince() == null) {
                continue;
            }

            // 安全获取用户信息，不再设置默认值
            String userId = log.getUserId();
            String userName = log.getUserName();
            String sex = log.getSex();
            String province = log.getProvince();

            // 解析课程ID
            String courseId = extractCourseId(log.getRequestUri());
            if (courseId == null) {
                continue; // 无效的课程URL，跳过
            }

            // 更新用户画像
            userProfileMap.computeIfAbsent(userId, k -> {
                UserProfile profile = new UserProfile();
                profile.setUserId(Long.valueOf(userId));
                profile.setUserName(userName);
                profile.setSex("男".equals(sex) ? 0 : 1);
                profile.setProvince(province);
                profile.setCourseLabels(new ArrayList<>());
                //TODO 先写死免费课程
                profile.setFreeLabel(0);
                return profile;
            });

            // 更新用户常访问课程ID
            List<Long> courseLabels = userProfileMap.get(userId).getCourseLabels();
            if (!courseLabels.contains(Long.valueOf(courseId))) {
                if (courseLabels.size() >= 5) {
                    courseLabels.remove(0);
                }
                courseLabels.add(Long.valueOf(courseId));
            }

            // 更新课程画像
            courseProfileMap.computeIfAbsent(courseId, k -> {
                CourseProfile profile = new CourseProfile();
                profile.setCourseId(Long.valueOf(courseId));
                profile.setSexLabel("");
                profile.setProvinceLabels(new ArrayList<>());
                return profile;
            });

            // 更新课程访问用户的性别标签
            String sexLabel = courseProfileMap.get(courseId).getSexLabel();
            if (!sexLabel.contains(sex)) {
                if (sexLabel.length() > 0) {
                    sexLabel += ",";
                }
                sexLabel += sex;
                courseProfileMap.get(courseId).setSexLabel(sexLabel);
            }

            // 更新课程访问用户的省份标签
            List<String> provinceLabels = courseProfileMap.get(courseId).getProvinceLabels();
            if (!provinceLabels.contains(province)) {
                if (provinceLabels.size() >= 5) {
                    provinceLabels.remove(0);
                }
                provinceLabels.add(province);
            }
        }

        result.setUserProfiles(new ArrayList<>(userProfileMap.values()));
        result.setCourseProfiles(new ArrayList<>(courseProfileMap.values()));

        // 保存分析结果
        saveProfilesToDB(result);

        return result;
    }

    /**
     * 将用户画像和课程画像保存到数据库
     */
    @Transactional
    public void saveProfilesToDB(LogAnalysisResult result) {
        List<UserProfile> userProfiles = result.getUserProfiles();
        for (UserProfile userProfile : userProfiles) {
            // 使用Service方法查询用户画像
            UserProfile existingUserProfile = userProfileService.getOne(
                    new LambdaQueryWrapper<UserProfile>()
                            .eq(UserProfile::getUserId, userProfile.getUserId())
            );

            if (existingUserProfile != null) {
                userProfile.setId(existingUserProfile.getId());
                // 使用Service方法更新用户画像
                userProfileService.updateById(userProfile);
            } else {
                // 使用Service方法插入用户画像
                userProfileService.save(userProfile);
            }
        }

        List<CourseProfile> courseProfiles = result.getCourseProfiles();
        for (CourseProfile courseProfile : courseProfiles) {
            CourseSearchDTO searchInfo = courseClient.getSearchInfo(courseProfile.getCourseId());// 使用Service方法查询课程画像
            if(searchInfo == null){
                continue;
            }
            CourseProfile existingCourseProfile = courseProfileService.getOne(
                    new LambdaQueryWrapper<CourseProfile>()
                            .eq(CourseProfile::getCourseId, courseProfile.getCourseId())
            );
            if (existingCourseProfile != null) {
                courseProfile.setId(existingCourseProfile.getId());
                // 使用Service方法更新课程画像
                courseProfileService.updateById(courseProfile);
            } else {
                // 使用Service方法插入课程画像
                courseProfileService.save(courseProfile);
            }
        }
    }

    /**
     * 从请求URI中提取课程ID
     * 示例：/cs/courses/baseInfo/8 -> 8
     */
    private String extractCourseId(String requestUri) {
        if (requestUri == null || !requestUri.startsWith("/cs/courses/baseInfo/")) {
            return null;
        }

        try {
            // 提取最后一个斜杠后的部分作为课程ID
            int lastSlashIndex = requestUri.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < requestUri.length() - 1) {
                return requestUri.substring(lastSlashIndex + 1);
            }
        } catch (Exception e) {
            // 记录异常但不中断处理
            System.err.println("Failed to extract course ID from URI: " + requestUri);
        }

        return null;
    }

    @Override
    public PageDTO<UserProfileVO> getAnalysisResultByUser(PageQuery query) {
        // 1.分页条件
        Page<UserProfile> profilePage = new Page<>(query.getPageNo(), query.getPageSize());
        Page<UserProfile> page = userProfileService.lambdaQuery().page(profilePage);

        Set<Long> userIds = new HashSet<>();
        for (UserProfile record : page.getRecords()) {
            userIds.add(record.getUserId());
        }
        List<UserDTO> userDTOs = userClient.queryUserByIds(userIds);
        Map<Long, String> userMap = CollUtils.isEmpty(userDTOs) ?
                new HashMap<>() :
                userDTOs.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getIcon));
        List<UserProfileVO> list = page.getRecords().stream().map(profile -> {
            UserProfileVO vo = new UserProfileVO();
            BeanUtils.copyProperties(profile, vo);
            vo.setIcon(userMap.get(profile.getUserId()));
            return vo;
        }).collect(Collectors.toList());

        return PageDTO.of(page, list);
    }

    @Override
    public PageDTO<CourseProfileVO> getAnalysisResultByCourse(PageQuery query) {
        Page<CourseProfile> profilePage = new Page<>(query.getPageNo(), query.getPageSize());
        Page<CourseProfile> page = courseProfileService.lambdaQuery().page(profilePage);
        List<CourseProfile> records = page.getRecords();
        // 课程id
        Set<Long> courseIds = new HashSet<>();
        for (CourseProfile record : records) {
            courseIds.add(record.getCourseId());
        }
        // 获取课程信息列表
        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        // 将课程信息列表转换为 Map<Long, CourseSimpleInfoDTO>
        Map<Long, CourseSimpleInfoDTO> courseMap = courseInfos.stream()
                .collect(Collectors.toMap(
                        CourseSimpleInfoDTO::getId,  // 键：课程ID
                        info -> info,                // 值：课程信息对象本身
                        (existing, replacement) -> existing  // 处理键冲突的策略（保留现有值）
                ));

        // 转换为VO
        List<CourseProfileVO> list = records.stream().map(courseProfile -> {
            CourseProfileVO courseProfileVO = BeanUtil.copyProperties(courseProfile, CourseProfileVO.class);

            // 从Map中获取课程信息
            CourseSimpleInfoDTO courseInfo = courseMap.get(courseProfile.getCourseId());
            if (courseInfo != null) {
                courseProfileVO.setName(courseInfo.getName());
                courseProfileVO.setCoverUrl(courseInfo.getCoverUrl());
                //保留两位小数  安全地处理价格转换
                if (courseInfo.getPrice() != null) {
                    BigDecimal priceInYuan = new BigDecimal(courseInfo.getPrice())
                            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                    courseProfileVO.setPrice(priceInYuan.doubleValue());
                }
                courseProfileVO.setFree(courseInfo.getFree());
            }
            return courseProfileVO;
        }).collect(Collectors.toList());

        return PageDTO.of(page, list);
    }

    /**
     * 获取时间范围
     */
    private TimeRange getTimeRange(FlowQuery query) {
        String beginTime = TimeHandlerUtils.getSevenDaysAgoTime().getBegin();
        String endTime = TimeHandlerUtils.getTodayTime().getEnd();

        if (query.getBeginTime() != null && query.getEndTime() != null) {
            beginTime = TimeHandlerUtils.localDateTimeToString(query.getBeginTime(), null);
            endTime = TimeHandlerUtils.localDateTimeToString(query.getEndTime(), null);
        }

        return new TimeRange(beginTime, endTime);
    }
}