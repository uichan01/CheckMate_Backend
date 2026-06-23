package com.CheckMate.checkmate_server.study.task.ai.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.task.ai.dto.res.AiFeedbackResponse;
import com.CheckMate.checkmate_server.study.task.ai.service.TaskAiFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study/task/ai-feedback")
@RequiredArgsConstructor
public class TaskAIFeedbackController {

    private final TaskAiFeedbackService taskAiFeedbackService;

    // AI 피드백 조회 (PENDING/PROCESSING 상태면 status만 반환, COMPLETED면 전체 내용 반환)
    @GetMapping("/{submissionId}")
    public ApiResponse<AiFeedbackResponse> getFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long submissionId
    ) {
        AiFeedbackResponse response = taskAiFeedbackService.getFeedback(
                userDetails.getUserId(), submissionId);
        return ApiResponse.success(response);
    }
}
