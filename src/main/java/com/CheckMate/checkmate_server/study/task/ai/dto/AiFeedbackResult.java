package com.CheckMate.checkmate_server.study.task.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiFeedbackResult {
    private String strength;
    private String weakness;
    private String suggestion;
}
