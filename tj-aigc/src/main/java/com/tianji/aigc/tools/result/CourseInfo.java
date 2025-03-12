package com.tianji.aigc.tools.result;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.tianji.api.dto.course.CourseBaseInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseInfo {

    @JsonPropertyDescription("课程id")
    private Long id;
    @JsonPropertyDescription("课程名称")
    private String name;
    @JsonPropertyDescription("课程价格，单位为元，货币为人民币")
    private double price;
    @JsonPropertyDescription("课程学习有效期，单位：月")
    private Integer validDuration;
    @JsonPropertyDescription("适用人群，例如：初学者")
    private String usePeople;
    @JsonPropertyDescription("课程详细介绍")
    private String detail;
    @JsonPropertyDescription("课程的url，相对路径")
    private String url;

    /**
     * 将CourseBaseInfoDTO转换为CourseInfo对象
     *
     * @param courseBaseInfoDTO 课程基础信息数据传输对象（包含原始课程数据）
     * @return 转换后的课程信息实体对象（包含格式化后的价格和详情页URL）
     */
    public static CourseInfo of(CourseBaseInfoDTO courseBaseInfoDTO) {
        if (null == courseBaseInfoDTO) {
            return null;
        }
        // 基础对象属性拷贝（忽略转换错误）
        CourseInfo courseInfo = BeanUtil.toBeanIgnoreError(courseBaseInfoDTO, CourseInfo.class);

        // 价格格式化处理：分转元 -> 四舍五入保留两位小数 -> 默认值0.0
        courseInfo.setPrice(Optional.ofNullable(courseBaseInfoDTO.getPrice())
                .map(num -> num.doubleValue() / 100d)
                .map(num -> NumberUtil.round(num, 2).doubleValue())
                .orElse(0.0d));

        // 构建前端详情页访问路径（拼接课程ID参数）
        courseInfo.setUrl("/#/details/index?id=" + courseBaseInfoDTO.getId());

        return courseInfo;
    }


}
