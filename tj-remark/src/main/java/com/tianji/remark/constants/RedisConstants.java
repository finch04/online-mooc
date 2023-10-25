package com.tianji.remark.constants;

public interface RedisConstants {
    /*给业务点赞的用户集合的KEY前缀，后缀是业务id*/
    String LIKE_BIZ_KEY_PREFIX = "likes:set:biz:";
    /*业务点赞数统计的KEY前缀，后缀是业务类型*/
    String LIKE_COUNT_KEY_PREFIX = "likes:times:type:";
}