package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server._common.service.S3FileUploadService;
import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackResult;
import com.CheckMate.checkmate_server.study.task.ai.outbox.service.AiFeedbackOutboxService;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionAttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiFeedbackConsumerRecoveryTest {
    @Mock StringRedisTemplate redis;
    @Mock StreamOperations<String, Object, Object> stream;
    @Mock TaskAiFeedbackRepository feedback;
    @Mock TaskSubmissionAttachmentRepository attachments;
    @Mock S3FileUploadService s3;
    @Mock AiService ai;
    @Mock AiFeedbackOutboxService outbox;
    private AiFeedbackConsumer consumer;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach void setup() {
        consumer = new AiFeedbackConsumer(redis, mapper, feedback, attachments, s3, ai, outbox);
    }

    @AfterEach void shutdown() { consumer.shutdown(); }

    @Test void recognizesOnlyExistingGroupThroughWrappedCause() {
        assertTrue(AiFeedbackConsumer.isExistingGroup(new RedisSystemException("Error in execution",
                new RuntimeException("BUSYGROUP Consumer Group name already exists"))));
        assertFalse(AiFeedbackConsumer.isExistingGroup(new RedisSystemException("Connection failed", null)));
    }

    @Test void activeProcessingMessageIsNotAcknowledged() throws Exception {
        var active = new TaskAiFeedbackEntity();
        active.markProcessing();
        when(feedback.findById(1L)).thenReturn(Optional.of(active));
        process(0);
        verifyNoInteractions(redis, ai, outbox);
    }

    @Test void completedMessageIsAcknowledgedWithoutCallingAi() throws Exception {
        var completed = new TaskAiFeedbackEntity();
        completed.markCompleted("s", "w", "suggestion");
        when(feedback.findById(1L)).thenReturn(Optional.of(completed));
        when(redis.opsForStream()).thenReturn(stream);
        process(0);
        verify(stream).acknowledge(AiFeedbackProducer.STREAM_KEY, "ai-feedback-group", RecordId.of("1-0"));
        verifyNoInteractions(ai, outbox);
    }

    @Test void ackFailureAfterCompletionDoesNotScheduleAnotherAiAttempt() throws Exception {
        allowClaim();
        when(ai.generateFeedback(anyString(), anyString(), anyString(), anyString(), anyList(), anyString()))
                .thenReturn(mapper.readValue("{\"strength\":\"s\",\"weakness\":\"w\",\"suggestion\":\"t\"}", AiFeedbackResult.class));
        when(feedback.completeOwned(eq(1L), anyString(), any(), eq("s"), eq("w"), eq("t"))).thenReturn(1);
        when(redis.opsForStream()).thenReturn(stream);
        when(stream.acknowledge(AiFeedbackProducer.STREAM_KEY, "ai-feedback-group", RecordId.of("1-0")))
                .thenThrow(new RedisSystemException("ACK connection lost", null));
        assertThrows(RedisSystemException.class, () -> process(0));
        verifyNoInteractions(outbox);
        verify(feedback, never()).failOwned(anyLong(), anyString(), any(), any());
    }

    @Test void retryIsAcknowledgedOnlyAfterTransactionalOutboxSave() throws Exception {
        allowFailure();
        when(outbox.enqueueRetry(any(), anyString())).thenReturn(true);
        when(redis.opsForStream()).thenReturn(stream);
        process(0);
        var ordered = inOrder(outbox, stream);
        ordered.verify(outbox).enqueueRetry(argThat(message -> message.getRetryCount() == 1), anyString());
        ordered.verify(stream).acknowledge(AiFeedbackProducer.STREAM_KEY, "ai-feedback-group", RecordId.of("1-0"));
    }

    @Test void failedRetryPersistenceLeavesMessageUnacknowledged() throws Exception {
        allowFailure();
        when(outbox.enqueueRetry(any(), anyString())).thenThrow(new IllegalStateException("DB unavailable"));
        assertThrows(IllegalStateException.class, () -> process(0));
        verifyNoInteractions(redis);
    }

    @Test void finalAiFailurePersistsFailedBeforeAck() throws Exception {
        allowFailure();
        when(feedback.failOwned(eq(1L), anyString(), any(), eq("AI unavailable"))).thenReturn(1);
        when(redis.opsForStream()).thenReturn(stream);
        process(3);
        var ordered = inOrder(feedback, stream);
        ordered.verify(feedback).failOwned(eq(1L), anyString(), any(), eq("AI unavailable"));
        ordered.verify(stream).acknowledge(AiFeedbackProducer.STREAM_KEY, "ai-feedback-group", RecordId.of("1-0"));
        verifyNoInteractions(outbox);
    }

    private void allowClaim() {
        when(feedback.claimAvailable(eq(1L), anyString(), any(), any(), any(), eq(4))).thenReturn(1);
    }

    private void allowFailure() {
        allowClaim();
        when(ai.generateFeedback(anyString(), anyString(), anyString(), anyString(), anyList(), anyString()))
                .thenThrow(new RuntimeException("AI unavailable"));
    }

    private void process(int retries) throws Exception {
        var message = new AiFeedbackMessage(1L, null, "task", "", "submission", "", List.of(), retries);
        MapRecord<String, String, String> record = StreamRecords.string(
                Map.of("payload", mapper.writeValueAsString(message)))
                .withStreamKey(AiFeedbackProducer.STREAM_KEY).withId(RecordId.of("1-0"));
        ReflectionTestUtils.invokeMethod(consumer, "process", record);
    }
}
