package com.CheckMate.checkmate_server.study.group.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberRole;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupPatchRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupSearchRequest;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyMemberEmailRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupDetailResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupRequestResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupResponseDto;
import com.CheckMate.checkmate_server.study.group.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
        long studyGroupId = groupService.createStudyGroup(request, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(studyGroupId));
    }

    // 스터디 그룹 목록 검색
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StudyGroupResponseDto>>> searchStudyGroups(@ModelAttribute StudyGroupSearchRequest request) {
        List<StudyGroupResponseDto> response = groupService.searchStudyGroups(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 스터디 그룹 상세 조회
    @GetMapping("/{studyId}")
    public ResponseEntity<ApiResponse<StudyGroupDetailResponseDto>> getStudyGroupDetails(@PathVariable long studyId,
                                                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        StudyGroupDetailResponseDto responseDto = groupService.getStudyGroupDetails(studyId, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    // 스터디 그룹 수정
    @PatchMapping("/{studyId}")
    public ResponseEntity<ApiResponse<Long>> updateStudyGroup(@PathVariable Long studyId,
                                                              @Valid @RequestBody StudyGroupPatchRequestDto request,
                                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long studyGroupId = groupService.updateStudyGroup(studyId, request, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(studyGroupId));
    }
    // TODO: 스터디 그룹 삭제(ON DELETE CASCADE, 이후에 작성), Spring scheduler로 삭제 예약, 일괄 삭제 로직

    // 스터디 인원 추가
    @PostMapping("/{studyId}/members")
    public ResponseEntity<ApiResponse<Long>> addStudyMember(@PathVariable Long studyId, @Valid @RequestBody StudyMemberEmailRequestDto req,
                                                            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long studyMemberId = groupService.addStudyMember(studyId, req.getEmail(), customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(studyMemberId));
    }

    // 스터디 인원 삭제
    @DeleteMapping("/{studyId}/members")
    public ResponseEntity<ApiResponse<Long>> removeStudyMember(@PathVariable Long studyId, @Valid @RequestBody StudyMemberEmailRequestDto req,
                                                            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long removeUserId = groupService.removeStudyMember(studyId, req.getEmail(), customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(removeUserId));
    }
    // 내가 속한 스터디 그룹 목록 조회
    @GetMapping("/list/me")
    public ResponseEntity<ApiResponse<List<StudyGroupResponseDto>>> getMyStudyGroups(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<StudyGroupResponseDto> list = groupService.getMyStudyGroups(customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }
    // 스터디원 역할 변경
    @PatchMapping("{studyId}/member_role/{role}")
    public ResponseEntity<ApiResponse<Long>> setStudyMemberRole(@PathVariable Long studyId, @Valid @RequestBody StudyMemberEmailRequestDto req, @PathVariable StudyMemberRole role,
                                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long studyMemberId = groupService.setStudyMemberRole(studyId, req.getEmail(), role, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(studyMemberId));
    }
    // 스터디 그룹 신청
    @PostMapping("/{studyId}/request")
    public ResponseEntity<ApiResponse<StudyMemberStatus>> requestStudyGroup(@PathVariable Long studyId,
                                                               @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        StudyMemberStatus status = groupService.requestStudyGroup(studyId, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    // 스터디 그룹 신청 목록 조회
    @GetMapping("/{studyId}/request")
    public ResponseEntity<ApiResponse<List<StudyGroupRequestResponseDto>>> getStudyGroupRequestList(@PathVariable Long studyId,
                                                                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<StudyGroupRequestResponseDto> list = groupService.getStudyGroupRequestList(
                studyId, customUserDetails.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.success(list));
    }
    // 스터디 신청 승인
    @PostMapping("/{studyMemberId}/approval")
    public ResponseEntity<ApiResponse<String>> approveStudyGroupRequest(@PathVariable Long studyMemberId,
                                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        groupService.approveStudyGroupRequest(studyMemberId, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("성공"));
    }
    // 스터디 신청 거절
    @PostMapping("/{studyMemberId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectStudyGroupRequest(@PathVariable Long studyMemberId,
                                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        groupService.rejectStudyGroupRequest(studyMemberId, customUserDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("성공"));
    }

}
