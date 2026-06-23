package com.CheckMate.checkmate_server.study.task.ai.repository;

import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAiFeedbackRepository extends JpaRepository<TaskAiFeedbackEntity, Long> {
    Optional<TaskAiFeedbackEntity> findByTaskSubmissionEntity_SubmissionId(Long submissionId);
}
