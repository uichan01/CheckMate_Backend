package com.CheckMate.checkmate_server.study.meeting.repository;

import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {
    List<MeetingEntity> findByStudyGroupEntity_StudyId(Long studyId);

    Optional<MeetingEntity> findFirstByStudyGroupEntity_StudyIdAndMeetingDateGreaterThanEqualOrderByMeetingDateAsc(
            Long studyId,
            LocalDateTime now
      );
    List<MeetingEntity> findAllByStudyGroupEntity_StudyIdAndMeetingDateGreaterThanEqualAndMeetingDateLessThan(
            Long studyId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    List<MeetingEntity> findByUserEntity_UserId(Long userId);
}
