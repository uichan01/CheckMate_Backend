package com.CheckMate.checkmate_server.study.meeting.service;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingParticipantEntity;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingParticipateStatus;
import com.CheckMate.checkmate_server.study.dto.SimpleUserDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.CreateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.req.UpdateMeetingRequestDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingDetailResponseDto;
import com.CheckMate.checkmate_server.study.meeting.dto.res.MeetingListResponseDto;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingParticipantRepository;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public long createMeeting(CreateMeetingRequestDto createMeetingRequestDto, String username) {
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudy_StudyIdAndUser_Email(createMeetingRequestDto.getStudyId(), username)
                .orElseThrow(()->new IllegalArgumentException("올바르지 않은 요청입니다."));
        if(studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("가입 승인 되지 않는 사용자의 요청입니다.");

        MeetingEntity meeting = MeetingEntity.builder()
                .studyGroupEntity(studyMemberEntity.getStudyGroupEntity())
                .userEntity(studyMemberEntity.getUserEntity())
                .title(createMeetingRequestDto.getTitle())
                .content(createMeetingRequestDto.getContent())
                .meetingDate(createMeetingRequestDto.getMeetingTime())
                .meetingPlace(createMeetingRequestDto.getMeetingPlace())
                .build();


        MeetingEntity savedMeetingEntity = meetingRepository.save(meeting);

        return savedMeetingEntity.getMeetingId();
    }

    @Override
    @Transactional
    public long updateMeeting(UpdateMeetingRequestDto updateMeetingRequestDto, String username) {
        MeetingEntity meetingEntity = meetingRepository.findById(updateMeetingRequestDto.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("미팅을 찾을 수 없습니다."));

        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudy_StudyIdAndUser_Email(meetingEntity.getStudyGroupEntity().getStudyId(), username)
                .orElseThrow(()->new IllegalArgumentException("올바르지 않은 요청입니다."));
        if(studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("가입 승인 되지 않는 사용자의 요청입니다.");

        if(!meetingEntity.getUserEntity().getEmail().equals(username))
            throw new IllegalArgumentException("미팅 생성 유저만 미팅 수정이 가능합니다.");

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

        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudy_StudyIdAndUser_Email(meetingEntity.getStudyGroupEntity().getStudyId(), username)
                .orElseThrow(()->new IllegalArgumentException("올바르지 않은 요청입니다."));
        if(studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("가입 승인 되지 않는 사용자의 요청입니다.");

        if(!meetingEntity.getUserEntity().getEmail().equals(username))
            throw new IllegalArgumentException("미팅 생성 유저만 미팅 삭제가 가능합니다.");

        meetingParticipantRepository.deleteByMeetingEntity_MeetingId(meetingId);
        meetingRepository.delete(meetingEntity);

        return meetingEntity.getMeetingId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingListResponseDto> getMeetingList(long studyId, String username) {
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudy_StudyIdAndUser_Email(studyId, username)
                .orElseThrow(()->new IllegalArgumentException("올바르지 않은 요청입니다."));
        if(studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("가입 승인 되지 않는 사용자의 요청입니다.");

        return meetingRepository.findByStudy_StudyId(studyId).stream().map(meeting -> MeetingListResponseDto.builder()
                .meetingId(meeting.getMeetingId())
                .time(meeting.getMeetingDate())
                .title(meeting.getTitle())
                .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingDetailResponseDto getMeetingDetail(long meetingId, String username) {
        MeetingEntity meetingEntity = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("미팅을 찾을 수 없습니다."));

        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudy_StudyIdAndUser_Email(meetingEntity.getStudyGroupEntity().getStudyId(), username)
                .orElseThrow(()->new IllegalArgumentException("올바르지 않은 요청입니다."));
        if(studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("가입 승인 되지 않는 사용자의 요청입니다.");

        List<SimpleUserDto> participateUsers = meetingParticipantRepository.findByMeetingEntity_MeetingId(meetingId)
                .stream()
                .map(participantEntity -> SimpleUserDto.from(participantEntity.getUserEntity()))
                .toList();

        return MeetingDetailResponseDto.builder()
                .meetingId(meetingId)
                .creatorName(meetingEntity.getUserEntity().getNickname())
                .title(meetingEntity.getTitle())
                .content(meetingEntity.getContent())
                .meetingTime(meetingEntity.getMeetingDate())
                .createdAt(meetingEntity.getCreatedAt())
                .place(meetingEntity.getMeetingPlace())
                .participateUsers(participateUsers)
                .build();
    }

    @Override
    @Transactional
    public void participateMeeting(long meetingId, String username) {
        MeetingEntity meetingEntity = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("미팅을 찾을 수 없습니다."));

        StudyMemberEntity studyMemberEntity = studyMemberRepository
                .findByStudyGroupEntity_GroupIdAndUserEntity_Email(
                        meetingEntity.getStudyGroupEntity().getStudyId(),
                        username
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디의 멤버만 미팅에 참여할 수 있습니다."));

        if (studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING) {
            throw new IllegalArgumentException("가입 승인되지 않은 사용자는 미팅에 참여할 수 없습니다.");
        }

        boolean alreadyParticipated = meetingParticipantRepository
                .existsByMeetingEntity_MeetingIdAndUserEntity_Email(meetingId, username);

        if (alreadyParticipated) {
            throw new IllegalArgumentException("이미 참여한 미팅입니다.");
        }

        MeetingParticipantEntity meetingParticipantEntity = MeetingParticipantEntity.builder()
                .meetingEntity(meetingEntity)
                .userEntity(studyMemberEntity.getUserEntity())
                .status(MeetingParticipateStatus.NOT_ATTENDED)
                .build();

        meetingParticipantRepository.save(meetingParticipantEntity);
    }

    @Override
    @Transactional
    public void attendanceMeeting(long meetingId, String username) {
        MeetingEntity meetingEntity = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("미팅을 찾을 수 없습니다."));

        MeetingParticipantEntity participantEntity = meetingParticipantRepository
                .findByMeetingEntity_MeetingIdAndUserEntity_Email(meetingId, username)
                .orElseThrow(() -> new IllegalArgumentException("미팅 참여자만 출석할 수 있습니다."));

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime meetingStartTime = meetingEntity.getMeetingDate();
        LocalDateTime attendanceDeadline = meetingStartTime.plusMinutes(10);

        if (now.isBefore(meetingStartTime)) {
            throw new IllegalArgumentException("아직 출석할 수 없습니다.");
        }

        if (!now.isAfter(attendanceDeadline)) {
            participantEntity.updateStatus(MeetingParticipateStatus.PRESENT);
        } else {
            participantEntity.updateStatus(MeetingParticipateStatus.LATE);
        }
    }
}
