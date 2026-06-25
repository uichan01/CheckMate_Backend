package com.CheckMate.checkmate_server.study.meeting.repository;

import com.CheckMate.checkmate_server.study.meeting.domain.MeetingParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipantEntity, Long> {
    void deleteByMeetingEntity_MeetingId(Long meetingId);
    List<MeetingParticipantEntity> findByMeetingEntity_MeetingId(Long meetingId);
    boolean existsByMeetingEntity_MeetingIdAndUserEntity_Email(Long meetingId, String email);
    boolean existsByMeetingEntity_MeetingIdAndUserEntity_UserId(Long meetingId, Long userId);
    Optional<MeetingParticipantEntity> findByMeetingEntity_MeetingIdAndUserEntity_Email(
            Long meetingId,
            String email
    );
    Optional<MeetingParticipantEntity> findByMeetingEntity_MeetingIdAndUserEntity_UserId(
            Long meetingId,
            Long userId
    );
    void deleteByUserEntity_UserId(Long userId);
}
