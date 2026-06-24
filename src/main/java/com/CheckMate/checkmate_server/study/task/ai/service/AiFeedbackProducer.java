package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackProducer {
    static final String STREAM_KEY = "ai-feedback-stream";
    static final String MESSAGE_FIELD = "payload";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(AiFeedbackMessage message) {
        try {
            publishPayload(objectMapper.writeValueAsString(message), message.getFeedbackId());
        } catch (Exception e) {
            throw new IllegalStateException("AI feedback message serialization failed", e);
        }
    }

    public void publishPayload(String payload, Long feedbackId) {
        redisTemplate.opsForStream().add(
                StreamRecords.string(Map.of(MESSAGE_FIELD, payload)).withStreamKey(STREAM_KEY));
        log.info("AI feedback message published feedbackId={}", feedbackId);
    }
}
