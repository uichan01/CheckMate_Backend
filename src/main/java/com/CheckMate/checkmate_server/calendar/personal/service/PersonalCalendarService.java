package com.CheckMate.checkmate_server.calendar.personal.service;


import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarCreateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarUpdateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarDetailResponse;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarListResponse;

import java.util.List;

public interface PersonalCalendarService {

    // 개인 일정 추가
    void createPersonalCalendar(Long userId, PersonalCalendarCreateRequest request);

    // 개인 일정 수정
    void updatePersonalCalendar(Long userId, Long personalCalendarId, PersonalCalendarUpdateRequest request);

    // 개인 일정 삭제
    void deletePersonalCalendar(Long userId, Long personalCalendarId);

    // 개인 일정 목록 조회
    List<PersonalCalendarListResponse> getPersonalCalendarList(Long userId);

    // 개인 일정 상세 조회
    PersonalCalendarDetailResponse getPersonalCalendarDetail(Long userId, Long personalCalendarId);
}