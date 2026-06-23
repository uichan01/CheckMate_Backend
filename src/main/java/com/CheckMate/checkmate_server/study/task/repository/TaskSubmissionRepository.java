package com.CheckMate.checkmate_server.study.task.repository;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmissionEntity, Long> {
    void deleteAllByTaskEntity_TaskId(Long taskId);
    List<TaskSubmissionEntity> findAllByTaskEntity_TaskId(Long studyId);
}
