package com.CheckMate.checkmate_server.study.task.dto.res;

import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TaskListResponse {

    private Long taskId;
    private String title;
    private Long writerId;
    private LocalDateTime dueDate;

    public static TaskListResponse from(TaskEntity taskEntity) {
        return TaskListResponse.builder()
                .taskId(taskEntity.getTaskId())
                .writerId(taskEntity.getUserEntity().getUserId())
                .title(taskEntity.getTitle())
                .dueDate(taskEntity.getDueDate())
                .build();
    }
}