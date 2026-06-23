package com.CheckMate.checkmate_server.calendar.personal.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PersonalCalendarListResponse {

    private Long personalCalendarId;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}