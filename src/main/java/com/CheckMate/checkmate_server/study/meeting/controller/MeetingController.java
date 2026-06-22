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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/study/meeting")
public class MeetingController {

    private final MeetingService meetingService;

    //미팅 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createMeeting(@Valid @RequestBody CreateMeetingRequestDto createMeetingRequestDto,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long meetingId = meetingService.createMeeting(createMeetingRequestDto, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(meetingId));
    }
    //미팅 수정
    @PatchMapping
    public ResponseEntity<ApiResponse<Long>> updateMeeting(@Valid @RequestBody UpdateMeetingRequestDto updateMeetingRequestDto,
                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        long meetingId = meetingService.updateMeeting(updateMeetingRequestDto, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingId));
    }
    //미팅 삭제
    @DeleteMapping("/{meeting_id}")
    public ResponseEntity<ApiResponse<Long>> deleteMeeting(@RequestParam("meeting_id") long meetingId,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        meetingService.deleteMeeting(meetingId, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingId));
    }
    //미팅 목록 조회
    @GetMapping("/list/{study_id}")
    public ResponseEntity<ApiResponse<List<MeetingListResponseDto>>> getMeetingList(@RequestParam("study_id") long studyId,
                                                                                    @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<MeetingListResponseDto> meetingList = meetingService.getMeetingList(studyId, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingList));
    }
    //미팅 상세 조회
    @GetMapping("/detail/{meeting_id}")
    public ResponseEntity<ApiResponse<MeetingDetailResponseDto>> getMeetingDetail(@RequestParam("meeting_id") long meetingId,
                                                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        MeetingDetailResponseDto meetingDetail = meetingService.getMeetingDetail(meetingId, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(meetingDetail));
    }

    //미팅 참여
    @PostMapping("/participate/{meeting_id}")
    public ResponseEntity<ApiResponse<Void>> participateMeeting(@RequestParam("meeting_id") long meetingId,
                                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        meetingService.participateMeeting(meetingId, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }


    //미팅 출석
    @PostMapping("/attendance/{meeting_id}")
    public ResponseEntity<ApiResponse<Void>> attendanceMeeting(@RequestParam("meeting_id") long meetingId,
                                                               @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        meetingService.attendanceMeeting(meetingId, customUserDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }
}
