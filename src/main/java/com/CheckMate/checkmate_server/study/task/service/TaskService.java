package com.CheckMate.checkmate_server.study.task.service;

import com.CheckMate.checkmate_server.study.task.dto.req.TaskCreateRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskSubmitRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskUpdateRequest;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskListResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionListResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface TaskService {
    // 과제 추가
    void createTask(String username, TaskCreateRequest request);
    // 과제 수정
    void updateTask(String username, TaskUpdateRequest request);
    // 과제 삭제
    void deleteTask(String username, Long taskId);
    // 과제 목록 조회
    List<TaskListResponse> getTaskList(Long studyId);
    // 과제 상세 조회
    TaskDetailResponse getTaskDetail(Long taskId);
    // 과제 제출
    void submitTask(String username, Long taskId, TaskSubmitRequest request);
    // 특정 과제 제출 목록 조회
    List<TaskSubmissionListResponse> getSubmissionList(Long taskId);
    // 과제 제출 상세 조회
    TaskSubmissionDetailResponse getSubmissionDetail(Long submissionId);
}
