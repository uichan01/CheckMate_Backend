package com.CheckMate.checkmate_server.dashboard.controller;

import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardResponseDto;
import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.CheckMate.checkmate_server.dashboard.service.DashboardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary/{studyId}")
    public ResponseEntity<ApiResponse<DashboardResponseDto>> getStudyGroupDashboardSummary(@PathVariable Long studyId,
                                                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        DashboardResponseDto responseDto = dashboardService.getStudyGroupDashboardSummary(
                studyId,
                customUserDetails.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
