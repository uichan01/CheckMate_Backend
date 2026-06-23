package com.CheckMate.checkmate_server.study.task.ai.dto.res;

import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiFeedbackResponse {

    private Long id;
    private Long submissionId;
    private String status;
    private String strength;
    private String weakness;
    private String suggestion;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static AiFeedbackResponse from(TaskAiFeedbackEntity entity) {
        return AiFeedbackResponse.builder()
                .id(entity.getId())
                .submissionId(entity.getTaskSubmissionEntity().getSubmissionId())
                .status(entity.getStatus().name())
                .strength(entity.getStrength())
                .weakness(entity.getWeakness())
                .suggestion(entity.getSuggestion())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
