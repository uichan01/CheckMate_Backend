package com.CheckMate.checkmate_server.dashboard.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class DashboardMeetingResponseDto {
    // 미팅ID
    private Long meetingId;
    // 미팅 제목
    String title;
    // 미팅 시간
    LocalDateTime meetingTime;
    // 미팅 장소
    String place;
}
