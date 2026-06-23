package com.CheckMate.checkmate_server.calendar.personal.dto.res;

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

    private LocalDateTime endTime;

    private String place;

    private LocalDateTime createdAt;
}