package com.CheckMate.checkmate_server.study.group.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupSearchRequest;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupResponseDto;
import com.CheckMate.checkmate_server.study.group.service.StudyGroupService;
import com.CheckMate.checkmate_server.user.dto.res.SignUpResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study/group")
public class StudyGroupController {
    // Service
    private final StudyGroupService groupService;
    
    // 스터디 그룹 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createStudyGroup(@Valid @RequestBody StudyGroupRequestDto request,
                                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long studyGroupId = groupService.createStudyGroup(request, customUserDetails);
        return ResponseEntity.ok(ApiResponse.success(studyGroupId));
    }

    // 스터디 그룹 목록 검색
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StudyGroupResponseDto>>> searchStudyGroups(@ModelAttribute StudyGroupSearchRequest request) {
        List<StudyGroupResponseDto> response = groupService.searchStudyGroups(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
