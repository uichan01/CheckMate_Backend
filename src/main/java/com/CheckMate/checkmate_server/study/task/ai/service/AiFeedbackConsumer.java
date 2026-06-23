package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackResult;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackConsumer implements MessageListener {

    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TaskAiFeedbackRepository feedbackRepository;
    private final AiService aiService;

    @PostConstruct
    public void subscribe() {
        listenerContainer.addMessageListener(this, new ChannelTopic(AiFeedbackProducer.CHANNEL));
        log.info("AI 피드백 Redis 채널 구독 시작: {}", AiFeedbackProducer.CHANNEL);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        AiFeedbackMessage msg;
        try {
            msg = objectMapper.readValue(message.getBody(), AiFeedbackMessage.class);
        } catch (Exception e) {
            log.error("AI 피드백 메시지 파싱 실패", e);
            return;
        }

        log.info("AI 피드백 처리 시작 feedbackId={}", msg.getFeedbackId());

        TaskAiFeedbackEntity feedback = feedbackRepository.findById(msg.getFeedbackId())
                .orElseGet(() -> {
                    log.error("피드백 엔티티를 찾을 수 없음 feedbackId={}", msg.getFeedbackId());
                    return null;
                });

        if (feedback == null) return;

        feedback.markProcessing();
        feedbackRepository.save(feedback);

        try {
            AiFeedbackResult result = aiService.generateFeedback(
                    msg.getTaskTitle(),
                    msg.getTaskContent(),
                    msg.getSubmissionTitle(),
                    msg.getSubmissionContent()
            );
            feedback.markCompleted(result.getStrength(), result.getWeakness(), result.getSuggestion());
            feedbackRepository.save(feedback);
            log.info("AI 피드백 생성 완료 feedbackId={}", msg.getFeedbackId());
        } catch (Exception e) {
            log.error("AI 피드백 생성 실패 feedbackId={}", msg.getFeedbackId(), e);
            feedback.markFailed(e.getMessage());
            feedbackRepository.save(feedback);
        }
    }
}
