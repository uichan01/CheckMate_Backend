package com.CheckMate.checkmate_server.calendar.personal.dto.req;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PersonalCalendarCreateRequest {

    @NotBlank(message = "일정 제목은 필수입니다.")
    @Size(max = 30, message = "일정 제목은 30자 이하로 입력해주세요.")
    private String title;

    @Size(max = 1000, message = "일정 내용은 1000자 이하로 입력해주세요.")
    private String content;

    @NotNull(message = "시작 시간은 필수입니다.")
    @FutureOrPresent(message = "시작 시간은 현재 이후여야 합니다.")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalDateTime endTime;

    @Size(max = 50, message = "장소는 50자 이하로 입력해주세요.")
    private String place;
}