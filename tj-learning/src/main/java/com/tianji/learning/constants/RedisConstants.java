package com.tianji.learning.constants;

public interface RedisConstants {

    /**
     * 签到记录的key前缀  完整格式为 sign:uid:用户id:年月
     */
    String SIGN_RECORD_KEY_PREFIX="sign:uid:";
    /**
     * 积分排行榜的Key的前缀：boards:202301
     */
    String POINTS_BOARD_KEY_PREFIX = "boards:";


    /**
     * 课程分享链接
     */
    String SHORT_URL_PREFIX = "short:url:";      // 短码 -> 分享ID
    String SHARE_DETAIL_PREFIX = "share:detail:"; // 分享ID -> 详情
    long EXPIRE_TIME = 3 * 60 * 60; // 3小时
}
