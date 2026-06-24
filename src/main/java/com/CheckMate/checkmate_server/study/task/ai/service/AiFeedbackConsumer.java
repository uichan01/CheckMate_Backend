package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server._common.service.S3FileUploadService;
import com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus;
import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackResult;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.ai.outbox.service.AiFeedbackOutboxService;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionAttachmentEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionAttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackConsumer {

    private static final int MAX_ATTACHMENT_TEXT_LENGTH = 20_000;
    private static final int MAX_ATTACHMENT_TEXT_LENGTH_PER_FILE = 5_000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final String GROUP = "ai-feedback-group";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TaskAiFeedbackRepository feedbackRepository;
    private final TaskSubmissionAttachmentRepository attachmentRepository;
    private final S3FileUploadService s3FileUploadService;
    private final AiService aiService;
    private final AiFeedbackOutboxService outboxService;
    private final ExecutorService consumers = Executors.newFixedThreadPool(2);
    private volatile boolean running = true;

    @PostConstruct
    public void subscribe() {
        createConsumerGroup();
        consumers.submit(() -> consume("consumer-1"));
        consumers.submit(() -> consume("consumer-2"));
        log.info("AI feedback consumers started stream={} group={}", AiFeedbackProducer.STREAM_KEY, GROUP);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        consumers.shutdownNow();
    }

    private void createConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(
                    AiFeedbackProducer.STREAM_KEY, ReadOffset.from("0-0"), GROUP);
        } catch (RedisSystemException e) {
            if (e.getMessage() == null || !e.getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }
    }

    private void consume(String consumerName) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP, consumerName),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(AiFeedbackProducer.STREAM_KEY, ReadOffset.lastConsumed()));
                if (records != null) {
                    records.forEach(this::process);
                }
            } catch (Exception e) {
                if (running) {
                    log.error("AI feedback stream consumption failed consumer={}", consumerName, e);
                }
            }
        }
    }

    private void process(MapRecord<String, Object, Object> record) {
        AiFeedbackMessage message;
        try {
            Object payload = record.getValue().get(AiFeedbackProducer.MESSAGE_FIELD);
            message = objectMapper.readValue(String.valueOf(payload), AiFeedbackMessage.class);
        } catch (Exception e) {
            log.error("AI feedback message parsing failed recordId={}", record.getId(), e);
            acknowledge(record.getId());
            return;
        }

        int claimed = feedbackRepository.claimPending(message.getFeedbackId(), LocalDateTime.now());
        if (claimed == 0) {
            acknowledge(record.getId());
            return;
        }

        TaskAiFeedbackEntity feedback = feedbackRepository.findById(message.getFeedbackId())
                .orElseThrow(() -> new IllegalStateException("Claimed AI feedback not found"));

        try {
            AiFeedbackResult result = aiService.generateFeedback(
                    message.getTaskTitle(), message.getTaskContent(),
                    message.getSubmissionTitle(), message.getSubmissionContent(),
                    message.getAttachmentUrls(), buildAttachmentText(message.getSubmissionId()));
            feedback.markCompleted(result.getStrength(), result.getWeakness(), result.getSuggestion());
            feedbackRepository.save(feedback);
            acknowledge(record.getId());
            log.info("AI feedback completed feedbackId={}", message.getFeedbackId());
        } catch (Exception e) {
            log.error("AI feedback failed feedbackId={} retryCount={}",
                    message.getFeedbackId(), message.getRetryCount(), e);
            if (message.getRetryCount() < MAX_RETRY_COUNT) {
                outboxService.enqueueRetry(message.nextRetry());
            } else {
                feedback.markFailed(e.getMessage());
                feedbackRepository.save(feedback);
            }
            acknowledge(record.getId());
        }
    }

    private void acknowledge(RecordId recordId) {
        redisTemplate.opsForStream().acknowledge(AiFeedbackProducer.STREAM_KEY, GROUP, recordId);
    }

    private String buildAttachmentText(Long submissionId) {
        if (submissionId == null) return "";

        List<TaskSubmissionAttachmentEntity> attachments =
                attachmentRepository.findAllByTaskSubmissionEntity_SubmissionId(submissionId);
        StringBuilder builder = new StringBuilder();
        for (TaskSubmissionAttachmentEntity attachment : attachments) {
            if (!isSupportedTextAttachment(attachment) || builder.length() >= MAX_ATTACHMENT_TEXT_LENGTH) continue;
            try {
                String text = s3FileUploadService.downloadAsUtf8Text(attachment.getFileKey());
                if (!StringUtils.hasText(text)) continue;
                int remaining = MAX_ATTACHMENT_TEXT_LENGTH - builder.length();
                builder.append("\n[File: ").append(attachment.getOriginalFileName()).append("]\n")
                        .append(limitLength(text, Math.min(MAX_ATTACHMENT_TEXT_LENGTH_PER_FILE, remaining)))
                        .append("\n");
            } catch (Exception e) {
                log.warn("AI feedback attachment extraction failed attachmentId={}",
                        attachment.getTaskSubmissionAttachmentId(), e);
            }
        }
        return builder.toString();
    }

    private boolean isSupportedTextAttachment(TaskSubmissionAttachmentEntity attachment) {
        String contentType = attachment.getContentType();
        if (StringUtils.hasText(contentType)) {
            String type = contentType.toLowerCase(Locale.ROOT);
            if (type.startsWith("text/") || type.startsWith("application/json")
                    || type.startsWith("application/xml") || type.startsWith("application/javascript")
                    || type.startsWith("application/x-javascript") || type.startsWith("application/x-yaml")) {
                return true;
            }
        }
        String fileName = attachment.getOriginalFileName();
        if (!StringUtils.hasText(fileName)) return false;
        String name = fileName.toLowerCase(Locale.ROOT);
        return List.of(".txt", ".md", ".csv", ".tsv", ".json", ".xml", ".yml", ".yaml",
                ".java", ".c", ".cpp", ".h", ".hpp", ".py", ".js", ".ts", ".css",
                ".html", ".htm", ".sql", ".properties", ".log")
                .stream().anyMatch(name::endsWith);
    }

    private String limitLength(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "\n... [truncated]";
    }
}
