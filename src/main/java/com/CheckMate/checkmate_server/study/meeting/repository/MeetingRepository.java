package com.CheckMate.checkmate_server.study.meeting.repository;

import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {
    List<MeetingEntity> findByStudy_StudyId(Long studyId);
}
