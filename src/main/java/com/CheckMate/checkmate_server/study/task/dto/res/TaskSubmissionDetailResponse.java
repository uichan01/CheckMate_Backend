package com.CheckMate.checkmate_server.study.task.dto.res;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TaskSubmissionDetailResponse {

    private Long submissionId;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public static TaskSubmissionDetailResponse from(TaskSubmissionEntity submissionEntity) {
        return TaskSubmissionDetailResponse.builder()
                .submissionId(submissionEntity.getSubmissionId())
                .nickname(submissionEntity.getUserEntity().getNickname())
                .title(submissionEntity.getTitle())
                .content(submissionEntity.getContent())
                .createdAt(submissionEntity.getCreatedAt())
                .build();
    }
}