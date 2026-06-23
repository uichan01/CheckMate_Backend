package com.CheckMate.checkmate_server.study.group.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupSearchRequest;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupDetailResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupResponseDto;
import com.CheckMate.checkmate_server.study.group.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        long studyGroupId = groupService.createStudyGroup(request, customUserDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(studyGroupId));
    }

    // 스터디 그룹 목록 검색
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StudyGroupResponseDto>>> searchStudyGroups(@ModelAttribute StudyGroupSearchRequest request) {
        List<StudyGroupResponseDto> response = groupService.searchStudyGroups(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 스터디 그룹 상세 조회
    @GetMapping("/{study_id}")
    public ResponseEntity<ApiResponse<StudyGroupDetailResponseDto>> getStudyGroupDetails(@PathVariable long studyId) {
        StudyGroupDetailResponseDto responseDto = groupService.getStudyGroupDetails(studyId);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    // 스터디 그룹 수정
    @PatchMapping("/{study_id}")
    public ResponseEntity<ApiResponse<Long>> updateStudyGroup(@PathVariable Long studyId,
                                                              @Valid @RequestBody StudyGroupRequestDto request,
                                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long studyGroupId = groupService.updateStudyGroup(studyId, request, customUserDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(studyGroupId));
    }
    // 스터디 그룹 삭제(ON DELETE CASCADE, 이후에 작성)

    // 스터디 인원 추가


    // 스터디 인원 삭제



    // 내가 속한 스터디 그룹 목록 조회

    // 스터디원 역할 변경

    // 스터디 그룹 신청

    // 스터디 그룹 신청 목록 조회

    // 스터디 신청 승인

    // 스터디 신청 거절

}
