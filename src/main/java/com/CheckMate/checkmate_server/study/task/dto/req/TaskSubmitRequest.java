package com.CheckMate.checkmate_server.study.task.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TaskSubmitRequest {

    @NotBlank(message = "제출 제목은 필수입니다.")
    @Size(max = 255, message = "제출 제목은 255자 이하로 입력해주세요.")
    private String title;

    private String content;
}