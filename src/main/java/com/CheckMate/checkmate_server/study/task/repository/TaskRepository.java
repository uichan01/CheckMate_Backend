package com.CheckMate.checkmate_server.study.task.repository;

import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByStudyGroupEntity_StudyId(Long studyId);

    List<TaskEntity> findAllByStudyGroupEntity_StudyIdAndDueDateGreaterThanEqualAndDueDateLessThan(
            Long studyId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
