package com.CheckMate.checkmate_server.study.task.service;

import com.CheckMate.checkmate_server.study.task.dto.req.TaskCreateRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskSubmitRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskUpdateRequest;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskListResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionListResponse;

import java.util.List;

public interface TaskService {
    // 과제 추가
    void createTask(Long userId, TaskCreateRequest request);
    // 과제 수정
    void updateTask(Long userId, TaskUpdateRequest request);
    // 과제 삭제
    void deleteTask(Long userId, Long taskId);
    // 과제 목록 조회
    List<TaskListResponse> getTaskList(Long studyId);
    // 과제 상세 조회
    TaskDetailResponse getTaskDetail(Long taskId);
    // 과제 제출
    void submitTask(Long userId, Long taskId, TaskSubmitRequest request);
    // 특정 과제 제출 목록 조회
    List<TaskSubmissionListResponse> getSubmissionList(Long taskId);
    // 과제 제출 상세 조회
    TaskSubmissionDetailResponse getSubmissionDetail(Long submissionId);
}
