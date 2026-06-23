package com.CheckMate.checkmate_server.study.task.service;

import com.CheckMate.checkmate_server.study.task.dto.req.TaskCreateRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskSubmitRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskUpdateRequest;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskListResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionListResponse;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Override
    public void createTask(Long userId, TaskCreateRequest request) {

    }

    @Override
    public void updateTask(Long userId, TaskUpdateRequest request) {

    }

    @Override
    public void deleteTask(Long userId, Long taskId) {

    }

    @Override
    public List<TaskListResponse> getTaskList(Long studyId) {
        return List.of();
    }

    @Override
    public TaskDetailResponse getTaskDetail(Long taskId) {
        return null;
    }

    @Override
    public void submitTask(Long userId, Long taskId, TaskSubmitRequest request) {

    }

    @Override
    public List<TaskSubmissionListResponse> getSubmissionList(Long taskId) {
        return List.of();
    }

    @Override
    public TaskSubmissionDetailResponse getSubmissionDetail(Long submissionId) {
        return null;
    }
}
