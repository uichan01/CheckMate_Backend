package com.CheckMate.checkmate_server.study.meeting.service;

import com.CheckMate.checkmate_server.study.meeting.dto.req.CreateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.UpdateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingDetailResponseDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingListResponseDto;


import java.util.List;

public interface MeetingService {
    //미팅 추가
    public long createMeeting(CreateMeetingRequestDto createMeetingRequestDto, String username);
    //미팅 수정
    public long updateMeeting(UpdateMeetingRequestDto updateMeetingRequestDto, String username);
    //미팅 삭제
    public long deleteMeeting(long meetingId, String username);
    //미팅 목록 조회
    public List<MeetingListResponseDto> getMeetingList(long studyId, String username);
    //미팅 상세 조회
    public MeetingDetailResponseDto getMeetingDetail(long meetingId, String username);
    //미팅 참석
    public void participateMeeting(long meetingId, String username);
    //미팅 출석
    public void attendanceMeeting(long meetingId, String username);
}