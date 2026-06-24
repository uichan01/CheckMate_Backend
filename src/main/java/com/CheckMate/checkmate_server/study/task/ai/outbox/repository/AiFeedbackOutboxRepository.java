package com.CheckMate.checkmate_server.study.task.ai.outbox.repository;

import com.CheckMate.checkmate_server.study.task.ai.outbox.domain.AiFeedbackOutboxEntity;
import com.CheckMate.checkmate_server.study.task.ai.outbox.domain.AiFeedbackOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiFeedbackOutboxRepository extends JpaRepository<AiFeedbackOutboxEntity, Long> {
    List<AiFeedbackOutboxEntity> findTop100ByStatusOrderByIdAsc(AiFeedbackOutboxStatus status);
}
