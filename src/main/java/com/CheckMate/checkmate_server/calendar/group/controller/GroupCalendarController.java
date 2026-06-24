package com.CheckMate.checkmate_server.calendar.group.controller;


import com.CheckMate.checkmate_server.calendar.group.dto.res.GroupScheduleResponse;
import com.CheckMate.checkmate_server.calendar.group.service.GroupCalendarService;
import com.CheckMate.checkmate_server.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar/group")
public class GroupCalendarController {
    private final GroupCalendarService groupCalendarService;

    @GetMapping("/{studyId}/schedule")
    public ApiResponse<List<GroupScheduleResponse>> getGroupSchedules(
            @PathVariable Long studyId,
            @RequestParam("start_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam("end_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ApiResponse.success(
                groupCalendarService.getGroupSchedules(studyId, startDate, endDate)
        );
    }
}
