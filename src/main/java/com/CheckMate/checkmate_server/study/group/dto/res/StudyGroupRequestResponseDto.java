package com.CheckMate.checkmate_server.study.group.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupRequestResponseDto {
    private Long studyMemberId;
    private Long userId;
    LocalDateTime requestDate;
}
