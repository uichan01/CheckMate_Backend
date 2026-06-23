package com.CheckMate.checkmate_server.calendar.personal.domain;

import com.CheckMate.checkmate_server.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_schedules")
@Getter
@NoArgsConstructor
public class UserScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "title", length = 20, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserScheduleEntity(
            UserEntity userEntity,
            LocalDateTime startTime,
            String title,
            String content
    ) {
        this.userEntity = userEntity;
        this.startTime = startTime;
        this.title = title;
        this.content = content;
    }

    public void updateSchedule(
            LocalDateTime startTime,
            String title,
            String content
    ) {
        this.startTime = startTime;
        this.title = title;
        this.content = content;
    }
}