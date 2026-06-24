package com.CheckMate.checkmate_server.study.task.ai.repository;

import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.Optional;

public interface TaskAiFeedbackRepository extends JpaRepository<TaskAiFeedbackEntity, Long> {
    Optional<TaskAiFeedbackEntity> findByTaskSubmissionEntity_SubmissionId(Long submissionId);
    void deleteAllByTaskSubmissionEntity_SubmissionId(Long submissionId);

    @Modifying
    @Transactional
    @Query("""
            update TaskAiFeedbackEntity f
               set f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING,
                   f.startedAt = :startedAt
             where f.id = :feedbackId
               and f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PENDING
            """)
    int claimPending(@Param("feedbackId") Long feedbackId,
                     @Param("startedAt") LocalDateTime startedAt);
}
