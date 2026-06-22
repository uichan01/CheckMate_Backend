package com.CheckMate.checkmate_server.study.meeting.domain;

import jakarta.persistence.*;
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

    public MeetingEntity(Long meetingId, String title, String content, LocalDateTime meetingDate, String meetingPlace) {
        this.meetingId = meetingId;
        this.title = title;
        this.content = content;
        this.meetingDate = meetingDate;
        this.meetingPlace = meetingPlace;
    }
}
