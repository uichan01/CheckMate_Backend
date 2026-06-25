package com.CheckMate.checkmate_server.study.task.domain;

import com.CheckMate.checkmate_server.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_submissions")
@Getter
@NoArgsConstructor
public class TaskSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity taskEntity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TaskSubmissionEntity(
            String title,
            String content,
            TaskEntity taskEntity,
            UserEntity userEntity
    ) {
        this.title = title;
        this.content = content;
        this.taskEntity = taskEntity;
        this.userEntity = userEntity;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
