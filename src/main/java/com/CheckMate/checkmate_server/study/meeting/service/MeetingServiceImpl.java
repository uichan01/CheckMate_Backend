package com.CheckMate.checkmate_server.study.meeting.service;

import com.CheckMate.checkmate_server.study.meeting.dto.req.CreateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.DeleteMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.UpdateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingDetailResponseDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService{

    @Override
    public long createMeeting(CreateMeetingRequestDto createMeetingRequestDto, String username) {
        return 0;
    }

    @Override
    public long updateMeeting(UpdateMeetingRequestDto updateMeetingRequestDto, String username) {
        return 0;
    }

    @Override
    public long deleteMeeting(DeleteMeetingRequestDto deleteMeetingRequestDto, String username) {
        return 0;
    }

    @Override
    public List<MeetingListResponseDto> getMeetingList(long studyId, String username) {
        return List.of();
    }

    @Override
    public MeetingDetailResponseDto getMeetingDetail(long meetingId, String username) {
        return null;
    }

    @Override
    public void attendanceMeeting(long meetingId, String username) {

    }
}
