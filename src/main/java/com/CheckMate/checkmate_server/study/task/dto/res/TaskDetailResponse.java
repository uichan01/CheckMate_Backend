package com.CheckMate.checkmate_server.study.task.dto.res;

import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TaskDetailResponse {

    private Long taskId;
    private String title;
    private String content;
    private Long writerId;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;

    public static TaskDetailResponse from(TaskEntity taskEntity) {
        return TaskDetailResponse.builder()
                .taskId(taskEntity.getTaskId())
                .title(taskEntity.getTitle())
                .content(taskEntity.getContent())
                .writerId(taskEntity.getUserEntity().getUserId())
                .dueDate(taskEntity.getDueDate())
                .createdAt(taskEntity.getCreatedAt())
                .build();
    }
}