package com.CheckMate.checkmate_server.study.task.dto.res;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TaskSubmissionListResponse {

    private Long submissionId;
    private String nickname;
    private LocalDateTime createdAt;

    public static TaskSubmissionListResponse from(TaskSubmissionEntity submissionEntity) {
        return TaskSubmissionListResponse.builder()
                .submissionId(submissionEntity.getSubmissionId())
                .nickname(submissionEntity.getUserEntity().getNickname())
                .createdAt(submissionEntity.getCreatedAt())
                .build();
    }
}