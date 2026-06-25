package com.CheckMate.checkmate_server.study.task.dto.res;

import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionAttachmentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskSubmissionAttachmentResponse {
    private Long attachmentId;
    private String originalFileName;
    private String fileUrl;
    private String contentType;
    private String fileSize;

    public static TaskSubmissionAttachmentResponse from(TaskSubmissionAttachmentEntity attachmentEntity) {
        return TaskSubmissionAttachmentResponse.builder()
                .attachmentId(attachmentEntity.getTaskSubmissionAttachmentId())
                .originalFileName(attachmentEntity.getOriginalFileName())
                .fileUrl(attachmentEntity.getFileUrl())
                .contentType(attachmentEntity.getContentType())
                .fileSize(String.valueOf(attachmentEntity.getFileSize()))
                .build();
    }
}
