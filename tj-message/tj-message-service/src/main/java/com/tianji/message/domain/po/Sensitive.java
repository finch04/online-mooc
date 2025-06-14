package com.tianji.message.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("`sensitive`")
public class Sensitive {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;
    private String sensitives;
    private LocalDateTime createTime;
}    