package com.CheckMate.checkmate_server.study.group.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.study.group.service.StudyGroupService;
import com.CheckMate.checkmate_server.user.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.dto.res.SignUpResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/study/group")
@RequiredArgsConstructor
public class StudyGroupController {
    // Service
    private final StudyGroupService gropuService;

    @PostMapping
    public ApiResponse<SignUpResponseDto> createStudyGroup(@Valid @RequestBody SignUpRequestDto request) {
//        SignUpResponseDto response = userService.signUp(request);
        return null;
    }

}
