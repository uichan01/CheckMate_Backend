package com.CheckMate.checkmate_server.study.task.ai.outbox.service;

import com.CheckMate.checkmate_server.study.task.ai.outbox.domain.AiFeedbackOutboxEntity;
import com.CheckMate.checkmate_server.study.task.ai.outbox.domain.AiFeedbackOutboxStatus;
import com.CheckMate.checkmate_server.study.task.ai.outbox.repository.AiFeedbackOutboxRepository;
import com.CheckMate.checkmate_server.study.task.ai.service.AiFeedbackProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackOutboxPublisher {
    private final AiFeedbackOutboxRepository outboxRepository;
    private final AiFeedbackProducer producer;

    @Scheduled(fixedDelayString = "${ai.feedback.outbox.publish-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        List<AiFeedbackOutboxEntity> events = outboxRepository
                .findTop100ByStatusOrderByIdAsc(AiFeedbackOutboxStatus.PENDING);

        for (AiFeedbackOutboxEntity event : events) {
            try {
                producer.publishPayload(event.getPayload(), event.getFeedbackId());
                event.markPublished();
            } catch (Exception e) {
                event.increaseRetryCount();
                log.error("AI feedback outbox publish failed outboxId={} retryCount={}",
                        event.getId(), event.getRetryCount(), e);
            }
        }
    }
}
