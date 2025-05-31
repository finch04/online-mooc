package com.tianji.learning.mq;

import com.tianji.api.dto.msg.LikedTimesDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.learning.domain.po.Evaluation;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.po.Note;
import com.tianji.learning.service.IEvaluationService;
import com.tianji.learning.service.IInteractionReplyService;
import com.tianji.learning.service.INoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikedRecordListener {

    private final IInteractionReplyService replyService;
    private final INoteService noteService;
    private final IEvaluationService  evaluationService;

    /**
     * QA问答系统 消费者
     * @param
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "qa.liked.times.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.LIKE_RECORD_EXCHANGE,type = ExchangeTypes.TOPIC),
            key=MqConstants.Key.QA_LIKED_TIMES_KEY
    ))
    public void onQAMsg(List<LikedTimesDTO>  list){
        log.info("QA-LikedRecordListener监听到消息：{}",list);
        List<InteractionReply> replyList =new ArrayList<>();
        for (LikedTimesDTO dto : list) {
            InteractionReply reply =new InteractionReply();
            reply.setLikedTimes(dto.getLikedTimes());
            reply.setId(dto.getBizId());

            replyList.add(reply);
        }
        replyService.updateBatchById(replyList);
    }


    /**
     * NOTE 笔记系统 消费者
     * @param
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "note.liked.times.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.LIKE_RECORD_EXCHANGE,type = ExchangeTypes.TOPIC),
            key=MqConstants.Key.NOTE_LIKED_TIMES_KEY
    ))
    public void onNoteMsg(List<LikedTimesDTO>  list){
        log.info("NOTE-LikedRecordListener监听到消息：{}",list);
        List<Note> noteList = new ArrayList<>();
        for (LikedTimesDTO dto : list) {
            Note note =new Note();
            note.setLikedTimes(dto.getLikedTimes());
            note.setId(dto.getBizId());

            noteList.add(note);
        }
        noteService.updateBatchById(noteList);
    }

    /**
     * COMMENT 评价系统 消费者
     * @param
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "comment.liked.times.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.LIKE_RECORD_EXCHANGE,type = ExchangeTypes.TOPIC),
            key=MqConstants.Key.COMMENT_HELPED_TIMES_KEY
    ))
    public void onCommentMsg(List<LikedTimesDTO>  list){
        log.info("COMMENT-LikedRecordListener监听到消息：{}",list);
        List<Evaluation> evaluations = new ArrayList<>();
        for (LikedTimesDTO dto : list) {
            Evaluation evaluation = new Evaluation();
            evaluation.setHelpCount(dto.getLikedTimes());
            evaluation.setId(dto.getBizId());

            evaluations.add(evaluation);
        }
        evaluationService.updateBatchById(evaluations);
    }


}
