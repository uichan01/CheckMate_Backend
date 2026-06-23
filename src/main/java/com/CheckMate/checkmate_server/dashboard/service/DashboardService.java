package com.CheckMate.checkmate_server.dashboard.service;

import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardResponseDto;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final MeetingRepository meetingRepository;
    private final StudyMemberRepository studyMemberRepository;

    public DashboardResponseDto getStudyGroupDashboardSummary(Long studyId, Long userId) {
        // 자신이 속한 스터디인지 우선 확인
        StudyMemberEntity myStudyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                studyId,
                userId,
                StudyMemberStatus.STATUS_ACTIVE
        ).orElseThrow(() -> new IllegalStateException("자신이 속한 스터디 그룹이 아닙니다."));

//        taskRepository.find
        // 가장 가까운 예정된 미팅을 가져옴
        // 없으면 null
        MeetingEntity meetingEntity = meetingRepository.findFirstByStudyGroupEntity_StudyIdAndMeetingDateGreaterThanEqualOrderByMeetingDateAsc(
                studyId,
                LocalDateTime.now()
        ).orElse(null);

        // 제출하지 않은 과제
        
        // 멤버별 참여율

        return null;
    }

}
