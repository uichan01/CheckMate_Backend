package com.CheckMate.checkmate_server.study.task.ai.domain;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_submission_ai_feedbacks")
@Getter
@NoArgsConstructor
public class TaskAiFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private TaskSubmissionEntity taskSubmissionEntity;

    @Column(name = "strength", columnDefinition = "TEXT")
    private String strength;

    @Column(name = "weakness", columnDefinition = "TEXT")
    private String weakness;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AiFeedbackStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "processing_token", length = 36)
    private String processingToken;

    @Column(name = "lease_until")
    private LocalDateTime leaseUntil;

    @Column(name = "processing_attempts", nullable = false, columnDefinition = "integer default 0")
    private int processingAttempts;

    @Builder
    public TaskAiFeedbackEntity(TaskSubmissionEntity taskSubmissionEntity) {
        this.taskSubmissionEntity = taskSubmissionEntity;
        this.status = AiFeedbackStatus.PENDING;
    }

    public void markProcessing() {
        this.status = AiFeedbackStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    public void markCompleted(String strength, String weakness, String suggestion) {
        this.status = AiFeedbackStatus.COMPLETED;
        this.strength = strength;
        this.weakness = weakness;
        this.suggestion = suggestion;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = AiFeedbackStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void markPendingForRetry() {
        this.status = AiFeedbackStatus.PENDING;
        this.errorMessage = null;
        this.startedAt = null;
        this.completedAt = null;
        this.processingToken = null;
        this.leaseUntil = null;
    }

    public void cleanUp(){
        this.status = AiFeedbackStatus.PENDING;
        this.strength = null;
        this.weakness = null;
        this.suggestion = null;
        this.errorMessage = null;
        this.startedAt = null;
        this.completedAt = null;
        this.processingToken = null;
        this.leaseUntil = null;
        this.processingAttempts = 0;
    }
}
