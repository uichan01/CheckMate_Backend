package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server._common.service.S3FileUploadService;
import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackResult;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionAttachmentEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionAttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackConsumer implements MessageListener {

    private static final int MAX_ATTACHMENT_TEXT_LENGTH = 20_000;
    private static final int MAX_ATTACHMENT_TEXT_LENGTH_PER_FILE = 5_000;

    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TaskAiFeedbackRepository feedbackRepository;
    private final TaskSubmissionAttachmentRepository attachmentRepository;
    private final S3FileUploadService s3FileUploadService;
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
                    msg.getSubmissionContent(),
                    msg.getAttachmentUrls(),
                    buildAttachmentText(msg.getSubmissionId())
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

    private String buildAttachmentText(Long submissionId) {
        if (submissionId == null) {
            return "";
        }

        List<TaskSubmissionAttachmentEntity> attachments =
                attachmentRepository.findAllByTaskSubmissionEntity_SubmissionId(submissionId);

        StringBuilder builder = new StringBuilder();
        for (TaskSubmissionAttachmentEntity attachment : attachments) {
            if (!isSupportedTextAttachment(attachment)) {
                continue;
            }
            if (builder.length() >= MAX_ATTACHMENT_TEXT_LENGTH) {
                break;
            }

            try {
                String text = s3FileUploadService.downloadAsUtf8Text(attachment.getFileKey());
                if (!StringUtils.hasText(text)) {
                    continue;
                }

                int remaining = MAX_ATTACHMENT_TEXT_LENGTH - builder.length();
                String truncated = limitLength(text, Math.min(MAX_ATTACHMENT_TEXT_LENGTH_PER_FILE, remaining));
                builder.append("\n[File: ")
                        .append(attachment.getOriginalFileName())
                        .append("]\n")
                        .append(truncated)
                        .append("\n");
            } catch (Exception e) {
                log.warn("AI feedback attachment text extraction failed attachmentId={}",
                        attachment.getTaskSubmissionAttachmentId(), e);
            }
        }

        return builder.toString();
    }

    private boolean isSupportedTextAttachment(TaskSubmissionAttachmentEntity attachment) {
        String contentType = attachment.getContentType();
        if (StringUtils.hasText(contentType)) {
            String lowerContentType = contentType.toLowerCase(Locale.ROOT);
            if (lowerContentType.startsWith("text/")
                    || lowerContentType.startsWith("application/json")
                    || lowerContentType.startsWith("application/xml")
                    || lowerContentType.startsWith("application/javascript")
                    || lowerContentType.startsWith("application/x-javascript")
                    || lowerContentType.startsWith("application/x-yaml")) {
                return true;
            }
        }

        String fileName = attachment.getOriginalFileName();
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        return lowerFileName.endsWith(".txt")
                || lowerFileName.endsWith(".md")
                || lowerFileName.endsWith(".csv")
                || lowerFileName.endsWith(".tsv")
                || lowerFileName.endsWith(".json")
                || lowerFileName.endsWith(".xml")
                || lowerFileName.endsWith(".yml")
                || lowerFileName.endsWith(".yaml")
                || lowerFileName.endsWith(".java")
                || lowerFileName.endsWith(".c")
                || lowerFileName.endsWith(".cpp")
                || lowerFileName.endsWith(".h")
                || lowerFileName.endsWith(".hpp")
                || lowerFileName.endsWith(".py")
                || lowerFileName.endsWith(".js")
                || lowerFileName.endsWith(".ts")
                || lowerFileName.endsWith(".css")
                || lowerFileName.endsWith(".html")
                || lowerFileName.endsWith(".htm")
                || lowerFileName.endsWith(".sql")
                || lowerFileName.endsWith(".properties")
                || lowerFileName.endsWith(".log");
    }

    private String limitLength(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "\n... [truncated]";
    }
}
