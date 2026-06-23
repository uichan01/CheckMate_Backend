package com.CheckMate.checkmate_server.calendar.personal.dto.res;

import com.CheckMate.checkmate_server.calendar.personal.domain.UserScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PersonalCalendarListResponse {

    private Long personalCalendarId;

    private String title;

    private LocalDateTime startTime;

    public static PersonalCalendarListResponse from(UserScheduleEntity userScheduleEntity) {
        return new PersonalCalendarListResponse(
                userScheduleEntity.getUserScheduleId(),
                userScheduleEntity.getTitle(),
                userScheduleEntity.getStartTime()
        );
    }
}