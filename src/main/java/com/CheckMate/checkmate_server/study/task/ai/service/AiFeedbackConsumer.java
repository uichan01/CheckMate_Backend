package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server._common.service.S3FileUploadService;
import com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisCallback;
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
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final ScheduledExecutorService heartbeats = Executors.newScheduledThreadPool(2);
    private final String instanceId = UUID.randomUUID().toString();

    @Value("${ai.feedback.recovery.idle-ms:180000}")
    private long recoveryIdleMs = 180_000;
    @Value("${ai.feedback.recovery.lease-ms:180000}")
    private long leaseMs = 180_000;
    @Value("${ai.feedback.recovery.heartbeat-ms:30000}")
    private long heartbeatMs = 30_000;
    private volatile boolean running = true;

    @PostConstruct
    public void subscribe() {
        if (recoveryIdleMs <= 0 || heartbeatMs <= 0 || leaseMs < heartbeatMs * 3) {
            throw new IllegalArgumentException("Recovery idle must be positive; lease must be at least 3 heartbeat intervals");
        }
        createConsumerGroup();
        consumers.submit(() -> consume(instanceId + "-1"));
        consumers.submit(() -> consume(instanceId + "-2"));
        log.info("AI feedback consumers started stream={} group={}", AiFeedbackProducer.STREAM_KEY, GROUP);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        consumers.shutdownNow();
        heartbeats.shutdownNow();
    }

    private void createConsumerGroup() {
        try {
            redisTemplate.execute((RedisCallback<String>) connection -> connection.streamCommands().xGroupCreate(
                    AiFeedbackProducer.STREAM_KEY.getBytes(StandardCharsets.UTF_8), GROUP, ReadOffset.from("0-0"), true));
        } catch (RedisSystemException e) {
            if (!isExistingGroup(e)) {
                throw e;
            }
        }
    }

    static boolean isExistingGroup(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().startsWith("BUSYGROUP ")) return true;
            if (cause.getCause() == cause) break;
        }
        return false;
    }

    private void consume(String consumerName) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                recoverPending(consumerName);
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
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void recoverPending(String consumerName) {
        // XPENDING IDLE + XCLAIM re-checks idle time atomically when ownership is transferred.
        // Claim one record just before processing, rather than leasing a large waiting batch.
        Duration minIdle = Duration.ofMillis(recoveryIdleMs);
        var pending = redisTemplate.opsForStream().pending(
                AiFeedbackProducer.STREAM_KEY, GROUP, Range.unbounded(), 10, minIdle);
        if (pending == null) return;
        for (var entry : pending) {
            if (!running || Thread.currentThread().isInterrupted()) return;
            List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream().claim(
                    AiFeedbackProducer.STREAM_KEY, GROUP, consumerName, minIdle, entry.getId());
            if (claimed != null) claimed.forEach(this::process);
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

        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        int claimed = feedbackRepository.claimAvailable(message.getFeedbackId(), token, now,
                now.plus(Duration.ofMillis(leaseMs)), now.minus(Duration.ofMillis(leaseMs)), MAX_RETRY_COUNT + 1);
        if (claimed == 0) {
            feedbackRepository.failExhausted(message.getFeedbackId(), now,
                    now.minus(Duration.ofMillis(leaseMs)), MAX_RETRY_COUNT + 1);
            acknowledgeIfTerminal(message.getFeedbackId(), record.getId());
            return;
        }

        var heartbeat = heartbeats.scheduleAtFixedRate(() -> renewLease(message.getFeedbackId(), token),
                heartbeatMs, heartbeatMs, TimeUnit.MILLISECONDS);
        try {
            AiFeedbackResult result;
            try {
                result = aiService.generateFeedback(
                        message.getTaskTitle(), message.getTaskContent(),
                        message.getSubmissionTitle(), message.getSubmissionContent(),
                        message.getAttachmentUrls(), buildAttachmentText(message.getSubmissionId()));
            } catch (Exception e) {
                log.error("AI feedback failed feedbackId={} retryCount={}",
                        message.getFeedbackId(), message.getRetryCount(), e);
                boolean persisted = message.getRetryCount() < MAX_RETRY_COUNT
                        ? outboxService.enqueueRetry(message.nextRetry(), token)
                        : feedbackRepository.failOwned(message.getFeedbackId(), token, LocalDateTime.now(), e.getMessage()) == 1;
                if (persisted) acknowledge(record.getId());
                return;
            }
            // Persistence/ACK failures leave the message pending; they are not AI-call failures.
            if (feedbackRepository.completeOwned(message.getFeedbackId(), token, LocalDateTime.now(),
                    result.getStrength(), result.getWeakness(), result.getSuggestion()) == 1) {
                acknowledge(record.getId());
                log.info("AI feedback completed feedbackId={}", message.getFeedbackId());
            }
        } finally {
            heartbeat.cancel(false);
        }
    }

    private void renewLease(Long feedbackId, String token) {
        try {
            LocalDateTime now = LocalDateTime.now();
            feedbackRepository.renewLease(feedbackId, token, now, now.plus(Duration.ofMillis(leaseMs)));
        } catch (Exception e) {
            log.warn("AI feedback lease renewal failed feedbackId={}", feedbackId, e);
        }
    }

    private void acknowledgeIfTerminal(Long feedbackId, RecordId recordId) {
        var feedback = feedbackRepository.findById(feedbackId);
        if (feedback.isEmpty() || feedback.get().getStatus() == AiFeedbackStatus.COMPLETED
                || feedback.get().getStatus() == AiFeedbackStatus.FAILED) {
            acknowledge(recordId);
        }
        // An active PROCESSING job must retain its pending message for crash recovery.
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
