package com.CheckMate.checkmate_server.study.meeting.domain;

import com.CheckMate.checkmate_server.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meeting_participants")
@Getter
@NoArgsConstructor
public class MeetingParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_participant_id")
    private Long meetingParticipantId;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private MeetingEntity meetingEntity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column
    @Enumerated(EnumType.STRING)
    private MeetingParticipateStatus status;

    @Builder
    public MeetingParticipantEntity(MeetingEntity meetingEntity, UserEntity userEntity, MeetingParticipateStatus status) {
        this.meetingEntity = meetingEntity;
        this.userEntity = userEntity;
        this.status = status;
    }
    public void updateStatus(MeetingParticipateStatus status) {
        this.status = status;
    }
}
