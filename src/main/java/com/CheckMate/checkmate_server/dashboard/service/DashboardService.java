package com.CheckMate.checkmate_server.dashboard.service;

import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardMeetingResponseDto;
import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardResponseDto;
import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardTaskResponseDto;
import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardUserResponseDto;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Transactional
    public DashboardResponseDto getStudyGroupDashboardSummary(Long studyId, Long userId) {
        // 자신이 속한 스터디인지 우선 확인
        boolean isMember = studyMemberRepository
                .existsByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                        studyId,
                        userId,
                        StudyMemberStatus.STATUS_ACTIVE
                );

        if (!isMember) {
            throw new IllegalStateException("자신이 속한 스터디 그룹이 아닙니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        // 가장 가까운 예정된 미팅을 가져옴
        // 없으면 null
        MeetingEntity meetingEntity = meetingRepository.findFirstByStudyGroupEntity_StudyIdAndMeetingDateGreaterThanEqualOrderByMeetingDateAsc(
                studyId,
                now
        ).orElse(null);

        // 제출하지 않은 과제
        List<TaskEntity> notSubmittedTasks = taskRepository.findUnsubmittedUpcomingTasksByStudyIdAndUserId(
                studyId,
                userId,
                now
        );

        // 멤버별 제출 과제 수
        List<DashboardUserResponseDto> dashboardUsers = studyMemberRepository.findUserSubmissionCountsByStudyId(
                studyId,
                StudyMemberStatus.STATUS_ACTIVE
        );
        long remainTaskCnt =  taskRepository.countByStudyGroupEntity_StudyId(studyId);
        DashboardResponseDto responseDto = DashboardResponseDto.builder()
                .remainTaskCnt(remainTaskCnt)
                .closeMeeting(
                        (meetingEntity != null) ? DashboardMeetingResponseDto.builder()
                        .meetingId(meetingEntity.getMeetingId())
                        .title(meetingEntity.getTitle())
                        .meetingTime(meetingEntity.getMeetingDate())
                        .place(meetingEntity.getMeetingPlace())
                        .build() : null)
                .remainTasks(notSubmittedTasks.stream().map(notSubmittedTask -> DashboardTaskResponseDto.builder()
                        .taskId(notSubmittedTask.getTaskId())
                        .title(notSubmittedTask.getTitle())
                        .dueDate(notSubmittedTask.getDueDate())
                        .build()
                ).toList())
                .members(dashboardUsers)
                .build();
        return responseDto;
    }
}
