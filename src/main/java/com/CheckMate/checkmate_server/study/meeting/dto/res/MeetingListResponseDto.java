package com.CheckMate.checkmate_server.study.meeting.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingListResponseDto {
    private long meetingId;
    private LocalDateTime time;
    private String title;
}
