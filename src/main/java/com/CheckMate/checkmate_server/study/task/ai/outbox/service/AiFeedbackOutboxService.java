package com.CheckMate.checkmate_server.study.task.ai.outbox.service;

import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.CheckMate.checkmate_server.study.task.ai.outbox.domain.AiFeedbackOutboxEntity;
import com.CheckMate.checkmate_server.study.task.ai.outbox.repository.AiFeedbackOutboxRepository;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiFeedbackOutboxService {
    private final AiFeedbackOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final TaskAiFeedbackRepository feedbackRepository;

    @Transactional
    public void enqueue(AiFeedbackMessage message) {
        try {
            outboxRepository.save(new AiFeedbackOutboxEntity(
                    message.getFeedbackId(), objectMapper.writeValueAsString(message)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("AI feedback outbox serialization failed", e);
        }
    }

    @Transactional
    public boolean enqueueRetry(AiFeedbackMessage message, String processingToken) {
        if (feedbackRepository.releaseForRetry(message.getFeedbackId(), processingToken, LocalDateTime.now()) == 0) {
            return false;
        }
        enqueue(message);
        return true;
    }
}
