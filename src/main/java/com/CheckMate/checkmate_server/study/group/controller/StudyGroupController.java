package com.CheckMate.checkmate_server.study.group.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study/group")
public class StudyGroupController {
    // Service
    private final StudyGroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createStudyGroup(@Valid @RequestBody StudyGroupRequestDto request) {
        long studyGroupId = groupService.createStudyGroup(request);
        return ResponseEntity.ok(ApiResponse.success(studyGroupId));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StudyGroupResponseDto>>> searchStudyGroups(@ModelAttribute StudyGroupSearchRequest request) {
        List<StudyGroupResponseDto> response = groupService.searchStudyGroups(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
