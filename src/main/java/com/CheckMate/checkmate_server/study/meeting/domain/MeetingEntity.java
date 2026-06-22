package com.CheckMate.checkmate_server.study.meeting.domain;

import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
@Getter
@NoArgsConstructor
public class MeetingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    @ManyToOne
    @JoinColumn(name = "study_id")
    private StudyGroupEntity studyGroupEntity;

    @ManyToOne
    @JoinColumn(name = "study_id")
    private UserEntity userEntity;

    @Column(name = "title", length = 20)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "meeting_date")
    private LocalDateTime meetingDate;

    @Column(name = "meeting_place", length = 50)
    private String meetingPlace;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MeetingEntity(StudyGroupEntity studyGroupEntity, UserEntity userEntity, String title, String content, LocalDateTime meetingDate, String meetingPlace) {
        this.studyGroupEntity = studyGroupEntity;
        this.userEntity = userEntity;
        this.title = title;
        this.content = content;
        this.meetingDate = meetingDate;
        this.meetingPlace = meetingPlace;
    }

    public void update(
            String title,
            String content,
            LocalDateTime meetingDate,
            String meetingPlace
    ) {
        this.title = title;
        this.content = content;
        this.meetingDate = meetingDate;
        this.meetingPlace = meetingPlace;
    }
}
