package com.CheckMate.checkmate_server.study.task.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TaskUpdateRequest {

    @NotNull(message = "과제 ID는 필수입니다.")
    private Long taskId;

    @NotBlank(message = "과제 제목은 필수입니다.")
    @Size(max = 255, message = "과제 제목은 255자 이하로 입력해주세요.")
    private String title;

    private String content;

    @NotNull(message = "마감일은 필수입니다.")
    private LocalDateTime dueDate;
}