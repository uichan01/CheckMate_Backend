package com.CheckMate.checkmate_server.scheduler;

import com.CheckMate.checkmate_server.calendar.personal.repository.PersonalCalendarRepository;
import com.CheckMate.checkmate_server.domain.DeleteStatus;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingParticipantEntity;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingParticipantRepository;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteScheduler {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final PersonalCalendarRepository personalCalendarRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final TaskAiFeedbackRepository taskAiFeedbackRepository;
    // 과제 첨부파일은 아직

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteDexpiredData() {
        LocalDateTime now = LocalDateTime.now();
        deleteExpiresUsers(now);
        deleteExpiresStudyGroups(now);
    }

    public void deleteExpiresStudyGroups(LocalDateTime now) {
        List<StudyGroupEntity> studyGroupEntityList = studyGroupRepository.findByStatusAndDeleteAtLessThanEqual(
                DeleteStatus.STATUS_DELETE_PENDING,
                now
        );

        studyGroupEntityList.stream().forEach(
                studyGroupEntity -> {
                    deleteRelatedToStudyGroup(studyGroupEntity.getStudyId());
                }
        );
        studyGroupRepository.deleteAll(studyGroupEntityList);

    }

    public void deleteExpiresUsers(LocalDateTime now) {
        List<UserEntity> userEntityList = userRepository.findByStatusAndDeleteAtLessThanEqual(
                DeleteStatus.STATUS_DELETE_PENDING,
                now
        );

        userEntityList.stream().forEach(userEntity -> {
            Long userId = userEntity.getUserId();

            // 내가 속한 study를 구해서 해당 스터디에서 작성한 나의 활동들 삭제
            List<StudyMemberEntity> studyMemberEntityList = studyMemberRepository.findByUserEntity_UserId(userId);
//            studyMemberEntityList.stream().forEach(studyMemberEntity -> {
//                Long studyId = studyMemberEntity.getStudyGroupEntity().getStudyId();
//                // 해당 스터디에 내가 등록한 과제와 제출물, 미팅 등이 있는지 확인
//
//                // 우선 해당 스터디의 모든 과제를 조회
//                List<TaskEntity> taskEntityList = taskRepository.findAllByStudyGroupEntity_StudyId(studyId);
//                taskEntityList.stream().forEach(taskEntity -> {
//
//                    // 내가 만든 과제라면 과제도 삭제
//                    if(taskEntity.getUserEntity().getUserId().equals(userId)) {
//                        // 해당 과제의 모든 제출물 획득
//                        List<TaskSubmissionEntity> taskSubmissionEntityList = taskSubmissionRepository.findAllByTaskEntity_TaskId(taskEntity.getTaskId());
//                        taskSubmissionEntityList.stream().forEach(taskSubmissionEntity -> {
//                            // 과제 제출과 연관된 테이블 삭제
//
//                            // AI 피드백 테이블 삭제
//                            taskAiFeedbackRepository.deleteAllByTaskSubmissionEntity_SubmissionId(taskSubmissionEntity.getSubmissionId());
//                        });
//                        taskSubmissionRepository.deleteAll(taskSubmissionEntityList);
//                        // 자신이 만든 과제 삭제
//                        taskRepository.delete(taskEntity);
//                    }
//                    else {
//                        // 해당 과제의 내가 제출한 과제 제출물 획득
//                        List<TaskSubmissionEntity> taskSubmissionEntityList = taskSubmissionRepository.findAllByTaskEntity_TaskIdAndUserEntity_UserId(taskEntity.getTaskId(), userId);
//                        taskSubmissionEntityList.stream().forEach(taskSubmissionEntity -> {
//                            // 과제 제출과 연관된 테이블 삭제
//
//                            // AI 피드백 테이블 삭제
//                            taskAiFeedbackRepository.deleteAllByTaskSubmissionEntity_SubmissionId(taskSubmissionEntity.getSubmissionId());
//                        });
//                        taskSubmissionRepository.deleteAll(taskSubmissionEntityList);
//                    }
//                });
//
//                // 스터디의 미팅을 조회
//                List<MeetingEntity> meetingEntityList = meetingRepository.findByStudyGroupEntity_StudyId(studyId);
//                meetingEntityList.stream().forEach(meetingEntity -> {
//                    // 내가 생성한 meeting이라면 바로 삭제
//                    if(meetingEntity.getUserEntity().getUserId().equals(userId)) {
//                        // 관련 데이터 삭제
//                        meetingParticipantRepository.deleteByMeetingEntity_MeetingId(meetingEntity.getMeetingId());
//                        // 자신 삭제
//                        meetingRepository.delete(meetingEntity);
//                    }
//                    // 혹은 자신의 미팅 참여 내역을 삭제
//                    else
//                        meetingParticipantRepository.deleteByMeetingEntity_MeetingIdAndUserEntity_UserId(meetingEntity.getMeetingId(), userId);
//
//                });
//            });

            // 과제 제출내역 획득
            List<TaskSubmissionEntity> taskSubmissionEntityList = taskSubmissionRepository.findAllByUserEntity_UserId(userId);
            taskSubmissionEntityList.stream().forEach(taskSubmissionEntity -> {
                // 관련 테이블 삭제

                // AI 테이블 삭제
                taskAiFeedbackRepository.deleteAllByTaskSubmissionEntity_SubmissionId(taskSubmissionEntity.getSubmissionId());
            });
            // 과제 제출물 삭제
            taskSubmissionRepository.deleteAll(taskSubmissionEntityList);

            // 내가 생성한 과제 조회
            List<TaskEntity> taskEntityList = taskRepository.findByUserEntity_UserId(userId);
            taskEntityList.stream().forEach(taskEntity -> {
                // 내가 생성한 과제에 대한 제출물 삭제
                List<TaskSubmissionEntity> taskSubmissionEntityList2 = taskSubmissionRepository.findAllByTaskEntity_TaskId(taskEntity.getTaskId());
                taskSubmissionEntityList2.stream().forEach(taskSubmissionEntity -> {
                    // 과제 제출 관련 테이블 삭제
                        
                    // AI테이블 삭제
                    taskAiFeedbackRepository.deleteAllByTaskSubmissionEntity_SubmissionId(taskSubmissionEntity.getSubmissionId());
                });
                taskSubmissionRepository.deleteAll(taskSubmissionEntityList2);
            });
            // 내 과제 삭제
            taskRepository.deleteAll(taskEntityList);

            // 미팅 참여에서 삭제
            meetingParticipantRepository.deleteByUserEntity_UserId(userId);
            // 내가 생성한 미팅 조회
            List<MeetingEntity> meetingEntityList = meetingRepository.findByUserEntity_UserId(userId);
            meetingEntityList.stream().forEach(meetingEntity -> {
                // 미팅 관련 테이블 삭제

                // 미팅 참여 테이블 삭제
                meetingParticipantRepository.deleteByMeetingEntity_MeetingId(meetingEntity.getMeetingId());
            });
            // 미팅에서 삭제
            meetingRepository.deleteAll(meetingEntityList);
            // 스터디 멤버 목록에서 삭제
            studyMemberRepository.deleteAll(studyMemberEntityList);
            // 개인 일정 삭제
            personalCalendarRepository.deleteByUserEntity_UserId(userId);
        });
        // 사용자 삭제
        userRepository.deleteAll(userEntityList);

    }

    private void deleteRelatedToStudyGroup(Long studyId) {
        // 스터디 멤버들 삭제
        studyMemberRepository.deleteByStudyGroupEntity_StudyId(studyId);
        // 스터디 id로 과제 테이블 조회
        List<TaskEntity> taskEntityList = taskRepository.findAllByStudyGroupEntity_StudyId(studyId);
        taskEntityList.stream()
                .forEach(taskEntity -> {
                    // 과제와 연관된 테이블 조회

                    // 과제 제출 테이블 조회
                    List<TaskSubmissionEntity> taskSubmissionEntityList = taskSubmissionRepository.findAllByTaskEntity_TaskId(taskEntity.getTaskId());
                    taskSubmissionEntityList.stream().forEach(taskSubmissionEntity -> {
                        // 과제 제출과 연관된 테이블 삭제

                        // ai피드백 테이블 삭제
                        taskAiFeedbackRepository.deleteAllByTaskSubmissionEntity_SubmissionId(taskSubmissionEntity.getSubmissionId());

                    });
                    // 과제 제출 테이블 삭제
                    taskSubmissionRepository.deleteAll(taskSubmissionEntityList);
                });
        // 과제 테이블 삭제
        taskRepository.deleteAll(taskEntityList);

        // 스터디 id로 미팅 조회
        List<MeetingEntity> meetingEntityList = meetingRepository.findByStudyGroupEntity_StudyId(studyId);
        meetingEntityList.stream().forEach(meetingEntity -> {
            // 미팅과 연관된 테이블 삭제

            // 미팅 참여 테이블 삭제
            meetingParticipantRepository.deleteByMeetingEntity_MeetingId(meetingEntity.getMeetingId());
        });
        // 미팅 테이블 삭제
        meetingRepository.deleteAll(meetingEntityList);
    }
}
