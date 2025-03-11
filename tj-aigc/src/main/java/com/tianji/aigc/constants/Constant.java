package com.tianji.aigc.constants;

public interface Constant {

    String USER_ID = "userId";
    String REQUEST_ID = "requestId";
    String STOP = "STOP";
    String ID = "id";

    interface Chats {
        String COMPLETE_TAG = "&complete&";
        String PARAM_TAG = "&param& {}";

        String CHAT_RESPONSE_METADATA = "chatResponseMetadata";
    }

    interface Tools {
        String QUERY_COURSE_BY_ID = "根据课程id查询";
        String PRE_PLACE_ORDER = "购买课程预下单操作，入参为多个课程id";
    }

    interface ToolParams {
        String COURSE_ID = "课程id";
        String COURSE_IDS = "课程id列表";
    }

}
