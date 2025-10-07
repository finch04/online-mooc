package com.tianji.live.domain.dto;

import lombok.Data;

// 关注/取消关注参数DTO
@Data
public class FollowDTO {
    // 前端传的 anchorId，属性名必须和前端参数名一致
    private Long anchorId;
    // 前端传的 isFollow，属性名必须和前端参数名一致
    private Boolean follow;

    public boolean isFollow() { // boolean类型的Getter是isXXX，不是getXXX
        return follow;
    }
}