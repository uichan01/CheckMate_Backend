package com.CheckMate.checkmate_server.calendar.personal.service;

import com.CheckMate.checkmate_server.calendar.personal.domain.UserScheduleEntity;
import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarCreateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.req.PersonalCalendarUpdateRequest;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarDetailResponse;
import com.CheckMate.checkmate_server.calendar.personal.dto.res.PersonalCalendarListResponse;
import com.CheckMate.checkmate_server.calendar.personal.repository.PersonalCalendarRepository;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalCalendarServiceImpl implements PersonalCalendarService{

    private final PersonalCalendarRepository personalCalendarRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createPersonalCalendar(Long userId, PersonalCalendarCreateRequest request) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserScheduleEntity userScheduleEntity = UserScheduleEntity.builder()
                .userEntity(userEntity)
                .startTime(request.getStartTime())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        personalCalendarRepository.save(userScheduleEntity);
    }

    @Override
    @Transactional
    public void updatePersonalCalendar(Long userId, Long personalCalendarId, PersonalCalendarUpdateRequest request) {
        UserScheduleEntity userScheduleEntity = personalCalendarRepository.findById(personalCalendarId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        UserEntity userEntity = userScheduleEntity.getUserEntity();

        if(userEntity.getUserId() != userId) {
            throw new IllegalArgumentException("본인의 일정만 수정할 수 있습니다.");
        }

        userScheduleEntity.updateSchedule(request.getStartTime(), request.getTitle(), request.getContent());
    }

    @Override
    public void deletePersonalCalendar(Long userId, Long personalCalendarId) {
        UserScheduleEntity userScheduleEntity = personalCalendarRepository.findById(personalCalendarId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        UserEntity userEntity = userScheduleEntity.getUserEntity();

        if(userEntity.getUserId() != userId) {
            throw new IllegalArgumentException("본인의 일정만 삭제할 수 있습니다.");
        }

        personalCalendarRepository.deleteById(personalCalendarId);
    }

    @Override
    public List<PersonalCalendarListResponse> getPersonalCalendarList(Long userId) {
        List<PersonalCalendarListResponse> list = personalCalendarRepository.findAllByUserEntity_UserId(userId)
                .stream()
                .map(PersonalCalendarListResponse::from)
                .toList();

        return list;
    }

    @Override
    public PersonalCalendarDetailResponse getPersonalCalendarDetail(Long userId, Long personalCalendarId) {
        UserScheduleEntity userScheduleEntity = personalCalendarRepository.findById(personalCalendarId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        UserEntity userEntity = userScheduleEntity.getUserEntity();

        if(userEntity.getUserId() != userId) {
            throw new IllegalArgumentException("본인의 일정만 조회할 수 있습니다.");
        }

        return PersonalCalendarDetailResponse.from(userScheduleEntity);
    }
}
