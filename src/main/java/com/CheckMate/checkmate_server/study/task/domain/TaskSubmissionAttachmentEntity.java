package com.CheckMate.checkmate_server.study.task.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_submission_attachments")
@Getter
@NoArgsConstructor
public class TaskSubmissionAttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long taskSubmissionAttachmentId;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private TaskSubmissionEntity taskSubmissionEntity;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "stored_file_name")
    private String storedFileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_key")
    private String fileKey;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public TaskSubmissionAttachmentEntity(
            TaskSubmissionEntity taskSubmissionEntity,
            String originalFileName,
            String storedFileName,
            String contentType,
            String fileKey,
            String fileUrl,
            Long fileSize
    ) {
        this.taskSubmissionEntity = taskSubmissionEntity;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
    }
}
