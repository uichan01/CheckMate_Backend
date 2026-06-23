package com.CheckMate.checkmate_server.study.task.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskCreateRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskSubmitRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskUpdateRequest;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskListResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionListResponse;
import com.CheckMate.checkmate_server.study.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/study/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 과제 추가
    @PostMapping
    public ApiResponse<Void> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        taskService.createTask(userDetails.getUsername(), request);
        return ApiResponse.success();
    }

    // 과제 수정
    @PatchMapping
    public ApiResponse<Void> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        taskService.updateTask(userDetails.getUsername(), request);
        return ApiResponse.success();
    }

    // 과제 삭제
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId
    ) {
        taskService.deleteTask(userDetails.getUsername(), taskId);
        return ApiResponse.success();
    }

    // 과제 목록 조회
    @GetMapping("/list/{studyId}")
    public ApiResponse<List<TaskListResponse>> getTaskList(
            @PathVariable Long studyId
    ) {
        List<TaskListResponse> response = taskService.getTaskList(studyId);
        return ApiResponse.success(response);
    }

    // 과제 상세 조회
    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailResponse> getTaskDetail(
            @PathVariable Long taskId
    ) {
        TaskDetailResponse response = taskService.getTaskDetail(taskId);
        return ApiResponse.success(response);
    }

    // 과제 제출
    @PostMapping("/{taskId}/submit")
    public ApiResponse<Void> submitTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskSubmitRequest request
    ) {
        taskService.submitTask(userDetails.getUsername(), taskId, request);
        return ApiResponse.success();
    }

    // 특정 과제 제출 목록 조회
    @GetMapping("/submission/{taskId}/list")
    public ApiResponse<List<TaskSubmissionListResponse>> getSubmissionList(
            @PathVariable Long taskId
    ) {
        List<TaskSubmissionListResponse> response = taskService.getSubmissionList(taskId);
        return ApiResponse.success(response);
    }

    // 과제 제출 상세 조회
    @GetMapping("/submission/{submissionId}")
    public ApiResponse<TaskSubmissionDetailResponse> getSubmissionDetail(
            @PathVariable Long submissionId
    ) {
        TaskSubmissionDetailResponse response = taskService.getSubmissionDetail(submissionId);
        return ApiResponse.success(response);
    }
}
