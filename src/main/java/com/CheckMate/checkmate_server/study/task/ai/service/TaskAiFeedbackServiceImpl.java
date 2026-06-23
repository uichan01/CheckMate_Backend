package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.res.AiFeedbackResponse;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskAiFeedbackServiceImpl implements TaskAiFeedbackService {

    private final TaskAiFeedbackRepository feedbackRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public AiFeedbackResponse getFeedback(Long userId, Long submissionId) {
        TaskSubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출물을 찾을 수 없습니다."));

        StudyMemberEntity member = studyMemberRepository
                .findByStudyGroupEntity_StudyIdAndUserEntity_UserId(
                        submission.getTaskEntity().getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 멤버만 조회할 수 있습니다."));

        if (member.getStatus() != StudyMemberStatus.STATUS_ACTIVE) {
            throw new IllegalArgumentException("스터디 활성 멤버만 조회할 수 있습니다.");
        }

        TaskAiFeedbackEntity feedback = feedbackRepository
                .findByTaskSubmissionEntity_SubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("AI 피드백이 존재하지 않습니다."));

        return AiFeedbackResponse.from(feedback);
    }
}
