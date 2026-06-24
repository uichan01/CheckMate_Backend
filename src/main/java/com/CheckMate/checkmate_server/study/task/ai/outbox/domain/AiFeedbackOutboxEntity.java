package com.CheckMate.checkmate_server.study.task.ai.outbox.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_feedback_outbox")
@Getter
@NoArgsConstructor
public class AiFeedbackOutboxEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiFeedbackOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public AiFeedbackOutboxEntity(Long feedbackId, String payload) {
        this.feedbackId = feedbackId;
        this.payload = payload;
        this.status = AiFeedbackOutboxStatus.PENDING;
    }

    public void markPublished() {
        status = AiFeedbackOutboxStatus.PUBLISHED;
        publishedAt = LocalDateTime.now();
    }

    public void increaseRetryCount() {
        retryCount++;
    }
}
