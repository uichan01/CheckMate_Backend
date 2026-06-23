package com.CheckMate.checkmate_server.study.task.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackMessage {
    private Long feedbackId;
    private String taskTitle;
    private String taskContent;
    private String submissionTitle;
    private String submissionContent;
}
