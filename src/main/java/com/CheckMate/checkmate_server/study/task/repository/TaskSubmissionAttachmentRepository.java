package com.CheckMate.checkmate_server.study.task.repository;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSubmissionAttachmentRepository extends JpaRepository<TaskSubmissionAttachmentEntity, Long> {
    List<TaskSubmissionAttachmentEntity> findAllByTaskSubmissionEntity_SubmissionId(Long submissionId);
    void deleteAllByTaskSubmissionEntity_SubmissionId(Long submissionId);
}
