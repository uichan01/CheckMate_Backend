package com.CheckMate.checkmate_server.study.meeting.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateMeetingRequestDto {
    
    LocalDateTime meetingTime;

    @Size(max = 50, message = "스터디 제목은 20자 이하여야 합니다.")
    String title;

    @NotBlank(message = "content는 필수 필드입니다.")
    String content;

    @Size(max = 50, message = "장소는 50자 이하여야 합니다.")
    String meetingPlace;
}
