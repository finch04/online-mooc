package com.tianji.common.constants;

public interface MqConstants {


    interface Exchange{

        /*数据中心有关的交换机*/
        String DATA_EXCHANGE = "data.topic";

        /*课程有关的交换机*/
        String COURSE_EXCHANGE = "course.topic";

        /*订单有关的交换机*/
        String ORDER_EXCHANGE = "order.topic";

        /*学习有关的交换机*/
        String LEARNING_EXCHANGE = "learning.topic";

        /*信息中心短信相关的交换机*/
        String SMS_EXCHANGE = "sms.direct";

        /*异常信息的交换机*/
        String ERROR_EXCHANGE = "error.topic";

        /*支付有关的交换机*/
        String PAY_EXCHANGE = "pay.topic";

        /*交易服务延迟任务交换机*/
        String TRADE_DELAY_EXCHANGE = "trade.delay.topic";

         /*点赞记录有关的交换机*/
        String LIKE_RECORD_EXCHANGE = "like.record.topic";

        /*促销服务的交换机*/
        String PROMOTION_EXCHANGE ="promotion.topic";

    }
    interface Queue {
        String ERROR_QUEUE_TEMPLATE = "error.{}.queue";
    }
    interface Key{
        /*数据有关的 RoutingKey*/
        String DATA_TODO_KEY = "data.todo";

        /*课程有关的 RoutingKey*/
        String COURSE_NEW_KEY = "course.new";
        String COURSE_UP_KEY = "course.up";
        String COURSE_DOWN_KEY = "course.down";
        String COURSE_EXPIRE_KEY = "course.expire";
        String COURSE_DELETE_KEY = "course.delete";
        String COURSE_DATA_KEY = "course.data";
        String COURSE_COMMENT_KEY = "course.comment";

        /*订单有关的RoutingKey*/
        String ORDER_PAY_KEY = "order.pay";
        String ORDER_REFUND_KEY = "order.refund";
        String ORDER_ANALYSIS_KEY = "order.analysis";
        String ORDER_DATA_KEY = "order.data";
        String ORDER_DETAIL_ANALYSIS_KEY = "order.detail.analysis";

        /*积分相关RoutingKey*/
        /* 写回答 */
        String WRITE_REPLY = "reply.new";
        /* 签到 */
        String SIGN_IN = "sign.in";
        /* 学习视频 */
        String LEARN_SECTION = "section.learned";
        /* 写笔记 */
        String WRITE_NOTE = "note.new";
        /* 笔记被采集 */
        String NOTE_GATHERED = "note.gathered";
        /* 评价课程 */
        String COURSE_COMMENT = "course.comment";

        /*点赞的RoutingKey*/
        String LIKED_TIMES_KEY_TEMPLATE = "{}.times.changed";
        /*问答*/
        String QA_LIKED_TIMES_KEY = "QA.times.changed";
        /*笔记*/
        String NOTE_LIKED_TIMES_KEY = "NOTE.times.changed";
        /*评价*/
        String COMMENT_HELPED_TIMES_KEY = "COMMENT.times.changed";

        /*短信系统发送短信*/
        String SMS_MESSAGE = "sms.message";

        /*异常RoutingKey的前缀*/
        String ERROR_KEY_PREFIX = "error.";
        String DEFAULT_ERROR_KEY = "error.#";

        /*支付有关的key*/
        String PAY_SUCCESS = "pay.success";
        String REFUND_CHANGE = "refund.status.change";

        String ORDER_DELAY_KEY = "delay.order.query";

        //领取优惠券的key
        String COUPON_RECEIVE ="coupon.receive";

        /**公告发送RoutingKey*/
        String NOTICE_SEND = "notice.send";

        /* 问题被回答 */
        String QUETSION_ANSWERED = "inbox.question.answered";
        /* 回答被评论 */
        String ANSWER_COMMENTED = "inbox.answer.commented";
        /* 评论被点赞 */
        String COMMENT_LIKED = "inbox.comment.liked";
        /* 笔记被点赞*/
        String NOTE_LIKED = "inbox.note.liked";
        /* 笔记被采集 */
        String NOTE_GATHERD = "inbox.note.gathered";
        /* 课程评价有人标记“有用” */
        String EVALUATION_USED = "inbox.evaluation.used";

        /* 订单支付成功消息*/
        String ORDER_PAY_SUCCESS = "inbox.order.pay.success";
        /* 订单支付失败消息*/
        String ORDER_PAY_FAIL = "inbox.order.pay.fail";
        /* 订单未付款自动取消 */
        String ORDER_AUTO_CANCEL = "inbox.order.auto.cancel";
        /* 订单退款成功通知 */
        String ORDER_REFUND_SUCCESS = "inbox.order.refund.success";
        /* 订单退款失败通知 */
        String ORDER_REFUND_FAIL = "inbox.order.refund.fail";

        /* 优惠券即将过期 */
        String COUPON_EXPIRE = "inbox.coupon.expire";
        /* 优惠券已过期*/
        String COUPON_EXPIRED = "inbox.coupon.expired";

        /* 用户注册成功*/
        String USER_REGISTER = "inbox.user.register";

        /* 排行榜结算 */
        String POINTS_BOARD_SETTLE = "inbox.points.board.settle";
        /* 积分快到期通知 */
        String POINTS_EXPIRE = "inbox.points.expire";
        /* 兑换商品发货通知 */
        String POINTS_EXCHANGE = "inbox.points.exchange";

        /* 直播开始提醒 */
        String LIVE_START = "inbox.live.start";

    }


    //rocketmq相关
    interface Topic {
        /* 消息有关topic */
        String MESSAGE_TOPIC = "message";

        /*学习有关topic*/
        String LEARN_TOPIC = "learn";

        /*QA:点赞记录有关topic*/
        String LIKE_RECORD_TOPIC = "like_record";

        /*订单有关topic*/
        String ORDER_TOPIC = "order";

        /*课程有关topic*/
        String COURSE_TOPIC = "course";

        /*促销服务有关topic*/
        String PROMOTION_TOPIC = "promotion";
    }

    interface Tag{

        /**
         * 消息发送有关tag
         */
        String MESSAGE_CODE = "code";

        /**
         * 用户课程有关tag
         */
        String USER_COURSE_DELETE = "delete";
        String USER_COURSE_SAVE = "save";

        /*订单有关*/
        String ORDER_PAY_TAG = "order.pay";
        String ORDER_REFUND_TAG = "order.refund";

    }

    interface ConsumerGroup{

        /**
         * 消息服务有关消费组
         */
        String MESSAGE_GROUP = "consumer_group_message";


        String SIGN_GROUP = "consumer_group_sign";
        String LIKED_RECORD_GROUP = "consumer_group_liked_record";

        /*用户课程有关消费组*/
        String USER_COURSE_DELETE = "consumer_group_delete_course";
        String USER_COURSE_SAVE = "consumer_group_save_course";

        /**
         * 用户优惠券相关消费组
         */
        String USER_COUPON_SAVE = "consumer_group_save_user_coupon";
        String USER_COUPON_DELETE = "consumer_group_delete_user_coupon";

        /**
         * 库存有关消费组
         */
        String COUPON_STOCK_REDUCE = "consumer_group_reduce_coupon_stock";
        String COUPON_STOCK_ADD = "consumer_group_add_coupon_stock";
    }
}
