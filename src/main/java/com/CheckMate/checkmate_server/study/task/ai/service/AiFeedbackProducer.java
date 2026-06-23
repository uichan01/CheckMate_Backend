package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackEvent;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackProducer {

    static final String CHANNEL = "ai-feedback";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // submitTask 트랜잭션 커밋 후 발행 → DB에 피드백 레코드가 확실히 저장된 뒤 Consumer가 읽음
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiFeedbackEvent(AiFeedbackEvent event) {
        publish(event.getMessage());
    }

    public void publish(AiFeedbackMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(CHANNEL, json);
            log.info("AI 피드백 메시지 발행 feedbackId={}", message.getFeedbackId());
        } catch (Exception e) {
            log.error("AI 피드백 메시지 발행 실패 feedbackId={}", message.getFeedbackId(), e);
            throw new RuntimeException("AI 피드백 메시지 발행 실패", e);
        }
    }
}
