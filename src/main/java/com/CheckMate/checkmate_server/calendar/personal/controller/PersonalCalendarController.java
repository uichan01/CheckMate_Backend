package com.CheckMate.checkmate_server.calendar.personal.controller;

import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarCreateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarUpdateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarDetailResponse;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarListResponse;
import com.CheckMate.checkmate_server.calendar.personal.service.PersonalCalendarService;
import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar/personal")
public class PersonalCalendarController {

    private final PersonalCalendarService personalCalendarService;

    // 개인 일정 추가
    @PostMapping
    public ApiResponse<Void> createPersonalCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PersonalCalendarCreateRequest request
    ) {
        personalCalendarService.createPersonalCalendar(userDetails.getUserId(), request);
        return ApiResponse.success();
    }

    // 개인 일정 수정
    @PatchMapping("/{personal_calendar_id}")
    public ApiResponse<Void> updatePersonalCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("personal_calendar_id") Long personalCalendarId,
            @RequestBody @Valid PersonalCalendarUpdateRequest request
    ) {
        personalCalendarService.updatePersonalCalendar(
                userDetails.getUserId(),
                personalCalendarId,
                request
        );
        return ApiResponse.success();
    }

    // 개인 일정 삭제
    @DeleteMapping("/{personal_calendar_id}")
    public ApiResponse<Void> deletePersonalCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("personal_calendar_id") Long personalCalendarId
    ) {
        personalCalendarService.deletePersonalCalendar(
                userDetails.getUserId(),
                personalCalendarId
        );
        return ApiResponse.success();
    }

    // 개인 일정 목록 조회
    @GetMapping("/list")
    public ApiResponse<List<PersonalCalendarListResponse>> getPersonalCalendarList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                personalCalendarService.getPersonalCalendarList(userDetails.getUserId())
        );
    }

    // 개인 일정 상세 조회
    @GetMapping("/{personal_calendar_id}")
    public ApiResponse<PersonalCalendarDetailResponse> getPersonalCalendarDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("personal_calendar_id") Long personalCalendarId
    ) {
        return ApiResponse.success(
                personalCalendarService.getPersonalCalendarDetail(
                        userDetails.getUserId(),
                        personalCalendarId
                )
        );
    }
}
