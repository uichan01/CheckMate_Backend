package com.CheckMate.checkmate_server.study.meeting.repository;

import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {
}
