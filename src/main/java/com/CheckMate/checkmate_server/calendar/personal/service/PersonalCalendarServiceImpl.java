package com.CheckMate.checkmate_server.calendar.personal.service;

import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarCreateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarUpdateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarDetailResponse;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarListResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalCalendarServiceImpl implements PersonalCalendarService{

    @Override
    public void createPersonalCalendar(Long userId, PersonalCalendarCreateRequest request) {

    }

    @Override
    public void updatePersonalCalendar(Long userId, Long personalCalendarId, PersonalCalendarUpdateRequest request) {

    }

    @Override
    public void deletePersonalCalendar(Long userId, Long personalCalendarId) {

    }

    @Override
    public List<PersonalCalendarListResponse> getPersonalCalendarList(Long userId) {
        return List.of();
    }

    @Override
    public PersonalCalendarDetailResponse getPersonalCalendarDetail(Long userId, Long personalCalendarId) {
        return null;
    }
}
