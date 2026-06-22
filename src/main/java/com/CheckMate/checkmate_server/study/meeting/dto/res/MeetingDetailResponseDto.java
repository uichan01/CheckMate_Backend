package com.CheckMate.checkmate_server.study.meeting.dto.res;

import com.CheckMate.checkmate_server.study.dto.SimpleUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDetailResponseDto {
    private long meetingId;
    private String creatorName;
    String title;
    String content;
    LocalDateTime meetingTime;
    LocalDateTime createdAt;
    String place;
    List<SimpleUserDto> participateUsers;
}
