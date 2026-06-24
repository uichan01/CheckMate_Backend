package com.CheckMate.checkmate_server.study.meeting.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.meeting.dto.req.CreateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.UpdateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingDetailResponseDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingListResponseDto;
import com.CheckMate.checkmate_server.study.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study/meeting")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createMeeting(
            @Valid @RequestBody CreateMeetingRequestDto createMeetingRequestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        long meetingId = meetingService.createMeeting(createMeetingRequestDto, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(meetingId));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Long>> updateMeeting(
            @Valid @RequestBody UpdateMeetingRequestDto updateMeetingRequestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        long meetingId = meetingService.updateMeeting(updateMeetingRequestDto, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingId));
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<Long>> deleteMeeting(
            @PathVariable long meetingId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        meetingService.deleteMeeting(meetingId, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingId));
    }

    @GetMapping("/list/{studyId}")
    public ResponseEntity<ApiResponse<List<MeetingListResponseDto>>> getMeetingList(
            @PathVariable long studyId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        List<MeetingListResponseDto> meetingList = meetingService.getMeetingList(studyId, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingList));
    }

    @GetMapping("/detail/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingDetailResponseDto>> getMeetingDetail(
            @PathVariable long meetingId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        MeetingDetailResponseDto meetingDetail = meetingService.getMeetingDetail(meetingId, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingDetail));
    }

    @PostMapping("/participate/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> participateMeeting(
            @PathVariable long meetingId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        meetingService.participateMeeting(meetingId, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PostMapping("/attendance/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> attendanceMeeting(
            @PathVariable long meetingId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        meetingService.attendanceMeeting(meetingId, customUserDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }
}
