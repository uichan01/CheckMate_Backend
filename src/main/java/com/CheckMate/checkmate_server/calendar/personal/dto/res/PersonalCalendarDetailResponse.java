package com.CheckMate.checkmate_server.calendar.personal.dto.res;

import com.CheckMate.checkmate_server.calendar.personal.domain.UserScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PersonalCalendarDetailResponse {

    private Long personalCalendarId;

    private String title;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime createdAt;

    public static PersonalCalendarDetailResponse from(UserScheduleEntity userScheduleEntity) {
        return new PersonalCalendarDetailResponse(
                userScheduleEntity.getUserScheduleId(),
                userScheduleEntity.getTitle(),
                userScheduleEntity.getContent(),
                userScheduleEntity.getStartTime(),
                userScheduleEntity.getCreatedAt()
        );
    }
}