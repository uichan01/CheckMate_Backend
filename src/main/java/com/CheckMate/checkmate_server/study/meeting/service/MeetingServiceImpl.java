package com.CheckMate.checkmate_server.study.meeting.service;

import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.dto.req.CreateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.UpdateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingDetailResponseDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingListResponseDto;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final StudyGroupRepository studyGroupRepository;

    @Override
    @Transactional
    public long createMeeting(CreateMeetingRequestDto createMeetingRequestDto, String username) {
        studyGroupRepository.exist

        MeetingEntity meeting = MeetingEntity.builder()
                .studyGroupEntity()
                .userEntity()
                .title(createMeetingRequestDto.getTitle())
                .content(createMeetingRequestDto.getContent())
                .meetingDate(createMeetingRequestDto.getMeetingTime())
                .meetingPlace(createMeetingRequestDto.getMeetingPlace())
                .build();

        MeetingEntity savedEntity = meetingRepository.save(meeting);
        return savedEntity.getMeetingId();
    }

    @Override
    @Transactional
    public long updateMeeting(UpdateMeetingRequestDto updateMeetingRequestDto, String username) {
        MeetingEntity meetingEntity = meetingRepository.findById(updateMeetingRequestDto.getMeetingId())
        .orElseThrow(() -> new IllegalArgumentException("미팅을 찾을 수 없습니다."));

        meetingEntity.update(updateMeetingRequestDto.getTitle()
                , updateMeetingRequestDto.getContent()
                , updateMeetingRequestDto.getMeetingTime()
                , updateMeetingRequestDto.getMeetingPlace());

        return meetingEntity.getMeetingId();
    }

    @Override
    @Transactional
    public long deleteMeeting(long meetingId, String username){
        MeetingEntity meetingEntity = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("미팅을 찾을 수 없습니다."));

        meetingRepository.delete(meetingEntity);

        return meetingEntity.getMeetingId();
    }

    @Override
    @Transactional
    public List<MeetingListResponseDto> getMeetingList(long studyId, String username) {
        List<MeetingListResponseDto> list = meetingRepository.findByStudy_StudyId(studyId).stream().map(meeting -> MeetingListResponseDto.builder()
                .meetingId(meeting.getMeetingId())
                .time(meeting.getMeetingDate())
                .title(meeting.getTitle())
                .build())
                .toList();
        return list;
    }

    @Override
    @Transactional
    public MeetingDetailResponseDto getMeetingDetail(long meetingId, String username) {
        return null;
    }

    @Override
    @Transactional
    public void attendanceMeeting(long meetingId, String username) {

    }
}
