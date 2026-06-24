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
                   f.startedAt = :now, f.processingToken = :token,
                   f.leaseUntil = :leaseUntil, f.processingAttempts = f.processingAttempts + 1
             where f.id = :feedbackId
               and f.processingAttempts < :maxAttempts
               and (f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PENDING
                    or (f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING
                        and (f.leaseUntil <= :now or (f.leaseUntil is null
                             and (f.startedAt is null or f.startedAt <= :legacyCutoff)))))
            """)
    int claimAvailable(@Param("feedbackId") Long feedbackId, @Param("token") String token,
                       @Param("now") LocalDateTime now, @Param("leaseUntil") LocalDateTime leaseUntil,
                       @Param("legacyCutoff") LocalDateTime legacyCutoff, @Param("maxAttempts") int maxAttempts);

    @Modifying
    @Transactional
    @Query("""
            update TaskAiFeedbackEntity f set f.leaseUntil = :leaseUntil
             where f.id = :feedbackId and f.processingToken = :token
               and f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING
               and f.leaseUntil > :now
            """)
    int renewLease(@Param("feedbackId") Long feedbackId, @Param("token") String token,
                   @Param("now") LocalDateTime now, @Param("leaseUntil") LocalDateTime leaseUntil);

    @Modifying
    @Transactional
    @Query("""
            update TaskAiFeedbackEntity f
               set f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.COMPLETED,
                   f.strength = :strength, f.weakness = :weakness, f.suggestion = :suggestion,
                   f.completedAt = :now, f.errorMessage = null, f.processingToken = null, f.leaseUntil = null
             where f.id = :feedbackId and f.processingToken = :token
               and f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING
               and f.leaseUntil > :now
            """)
    int completeOwned(@Param("feedbackId") Long feedbackId, @Param("token") String token,
                      @Param("now") LocalDateTime now, @Param("strength") String strength,
                      @Param("weakness") String weakness, @Param("suggestion") String suggestion);

    @Modifying
    @Transactional
    @Query("""
            update TaskAiFeedbackEntity f
               set f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PENDING,
                   f.processingToken = null, f.leaseUntil = null, f.startedAt = null,
                   f.completedAt = null, f.errorMessage = null
             where f.id = :feedbackId and f.processingToken = :token
               and f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING
               and f.leaseUntil > :now
            """)
    int releaseForRetry(@Param("feedbackId") Long feedbackId, @Param("token") String token,
                        @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("""
            update TaskAiFeedbackEntity f
               set f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.FAILED,
                   f.errorMessage = :error, f.completedAt = :now,
                   f.processingToken = null, f.leaseUntil = null
             where f.id = :feedbackId and f.processingToken = :token
               and f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING
               and f.leaseUntil > :now
            """)
    int failOwned(@Param("feedbackId") Long feedbackId, @Param("token") String token,
                  @Param("now") LocalDateTime now, @Param("error") String error);

    @Modifying
    @Transactional
    @Query("""
            update TaskAiFeedbackEntity f
               set f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.FAILED,
                   f.errorMessage = 'AI feedback processing attempt limit exceeded', f.completedAt = :now,
                   f.processingToken = null, f.leaseUntil = null
             where f.id = :feedbackId and f.processingAttempts >= :maxAttempts
               and (f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PENDING
                    or (f.status = com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus.PROCESSING
                        and (f.leaseUntil <= :now or (f.leaseUntil is null
                             and (f.startedAt is null or f.startedAt <= :legacyCutoff)))))
            """)
    int failExhausted(@Param("feedbackId") Long feedbackId, @Param("now") LocalDateTime now,
                      @Param("legacyCutoff") LocalDateTime legacyCutoff, @Param("maxAttempts") int maxAttempts);
}
