package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server.study.task.ai.dto.res.AiFeedbackResponse;

public interface TaskAiFeedbackService {
    AiFeedbackResponse getFeedback(Long userId, Long submissionId);
}
