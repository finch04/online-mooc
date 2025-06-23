package com.tianji.data.model.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Description：用户活跃数时段
 */
@Data
@NoArgsConstructor
@TableName("tab_dpv_time")
@ApiModel(value="DpvTime对象", description="用户活跃访问数时段分布")
public class DpvTime {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键 ID")
    private Long id;

    @ApiModelProperty(value = "0~7点活跃访问量数（不包括右区间）")
    private Long midNightDpv;

    @ApiModelProperty(value = "7~12点活跃访问量数（不包括右区间）")
    private Long noonDpv;

    @ApiModelProperty(value = "12~18点活跃访问量数（不包括右区间）")
    private Long afternoonDpv;

    @ApiModelProperty(value = "18~24点活跃访问量数（不包括右区间）")
    private Long eveningDpv;

    @ApiModelProperty(value = "统计时间【精确到时分秒】")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//set
    private LocalDateTime reportTime;

    // 创建时间:INSERT 代表只在插入时填充
    public LocalDateTime createTime;

    // 修改时间：INSERT_UPDATE 首次插入、其次更新时填充(或修改)
    public LocalDateTime updateTime;
}