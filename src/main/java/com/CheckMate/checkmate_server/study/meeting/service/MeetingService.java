package com.CheckMate.checkmate_server.study.meeting.service;

import com.CheckMate.checkmate_server.study.meeting.dto.req.CreateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.UpdateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingDetailResponseDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingListResponseDto;


import java.util.List;

public interface MeetingService {
    //미팅 추가
    long createMeeting(CreateMeetingRequestDto createMeetingRequestDto, Long userId);
    //미팅 수정
    long updateMeeting(UpdateMeetingRequestDto updateMeetingRequestDto, Long userId);
    //미팅 삭제
    long deleteMeeting(long meetingId, Long userId);
    //미팅 목록 조회
    List<MeetingListResponseDto> getMeetingList(long studyId, Long userId);
    //미팅 상세 조회
    MeetingDetailResponseDto getMeetingDetail(long meetingId, Long userId);
    //미팅 참석
    void participateMeeting(long meetingId, Long userId);
    //미팅 출석
    void attendanceMeeting(long meetingId, Long userId);
}