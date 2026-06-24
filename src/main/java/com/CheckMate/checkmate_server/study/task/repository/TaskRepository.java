package com.CheckMate.checkmate_server.study.task.repository;

import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByStudyGroupEntity_StudyId(Long studyId);

    // 단순한 미제출 과제
    @Query("""
        select t
        from TaskEntity t
        where t.studyGroupEntity.studyId = :studyId
          and not exists (
              select 1
              from TaskSubmissionEntity ts
              where ts.taskEntity = t
                and ts.userEntity.userId = :userId
          )
        order by t.dueDate asc
    """)
    List<TaskEntity> findUnsubmittedTasksByStudyIdAndUserId(
            @Param("studyId") Long studyId,
            @Param("userId") Long userId
    );

    // 마감일이 지나지 않은 미제출 과제
    @Query("""
    select t
    from TaskEntity t
    where t.studyGroupEntity.studyId = :studyId
      and t.dueDate >= :now
      and not exists (
          select 1
          from TaskSubmissionEntity ts
          where ts.taskEntity = t
            and ts.userEntity.userId = :userId
      )
    order by t.dueDate asc
""")
    List<TaskEntity> findUnsubmittedUpcomingTasksByStudyIdAndUserId(
            @Param("studyId") Long studyId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // 전체 과제 수 반환
    long countByStudyGroupEntity_StudyId(Long studyId);
    
    // 일정 기간의 과제 반환
    List<TaskEntity> findAllByStudyGroupEntity_StudyIdAndDueDateGreaterThanEqualAndDueDateLessThan(
            Long studyId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
