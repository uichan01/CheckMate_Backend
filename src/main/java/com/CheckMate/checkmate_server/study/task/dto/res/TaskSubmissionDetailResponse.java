package com.CheckMate.checkmate_server.study.task.dto.res;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionAttachmentEntity;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TaskSubmissionDetailResponse {

    private Long submissionId;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private List<TaskSubmissionAttachmentResponse> taskAttachmentResponses;

    public static TaskSubmissionDetailResponse from(TaskSubmissionEntity submissionEntity) {
        return from(submissionEntity, List.of());
    }

    public static TaskSubmissionDetailResponse from(TaskSubmissionEntity submissionEntity, List<TaskSubmissionAttachmentEntity> attachmentEntities) {
        return TaskSubmissionDetailResponse.builder()
                .submissionId(submissionEntity.getSubmissionId())
                .nickname(submissionEntity.getUserEntity().getNickname())
                .title(submissionEntity.getTitle())
                .content(submissionEntity.getContent())
                .createdAt(submissionEntity.getCreatedAt())
                .taskAttachmentResponses(attachmentEntities.stream()
                        .map(TaskSubmissionAttachmentResponse::from)
                        .toList())
                .build();
    }
}
