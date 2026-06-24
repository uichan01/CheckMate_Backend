package com.CheckMate.checkmate_server.study.task.service;

import com.CheckMate.checkmate_server._common.dto.FileUploadResult;
import com.CheckMate.checkmate_server._common.service.S3FileUploadService;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberRole;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionAttachmentEntity;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskCreateRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskSubmitRequest;
import com.CheckMate.checkmate_server.study.task.dto.req.TaskUpdateRequest;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskListResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionDetailResponse;
import com.CheckMate.checkmate_server.study.task.dto.res.TaskSubmissionListResponse;

import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackMessage;
import com.CheckMate.checkmate_server.study.task.ai.outbox.service.AiFeedbackOutboxService;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionAttachmentRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskRepository taskRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final TaskAiFeedbackRepository taskAiFeedbackRepository;
    private final TaskSubmissionAttachmentRepository taskSubmissionAttachmentRepository;
    private final S3FileUploadService s3FileUploadService;
    private final AiFeedbackOutboxService aiFeedbackOutboxService;

    @Override
    @Transactional
    public void createTask(Long userId, TaskCreateRequest taskCreateRequest) {
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskCreateRequest.getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 추가하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() == StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("과제를 추가하려는 스터디에 참여하고 있지 않습니다.");
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("과제를 추가하려는 스터디에 참여하고 있지 않습니다.");
        if(studyMemberEntity.getRole() == StudyMemberRole.ROLE_MEMBER)
            throw new IllegalArgumentException("일반 멤버는 과제를 추가할 수 없습니다..");

        TaskEntity taskEntity = TaskEntity.builder()
                .studyGroupEntity(studyMemberEntity.getStudyGroupEntity())
                .userEntity(studyMemberEntity.getUserEntity())
                .title(taskCreateRequest.getTitle())
                .content(taskCreateRequest.getContent())
                .dueDate(taskCreateRequest.getDueDate())
                .build();

        taskRepository.save(taskEntity);
    }

    @Override
    @Transactional
    public void updateTask(Long userId, TaskUpdateRequest taskUpdateRequest) {
        TaskEntity taskEntity = taskRepository.findById(taskUpdateRequest.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다.(task_id 잘못됨)"));
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskEntity.getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 수정하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("과제를 수정하려는 스터디에 참여하고 있지 않습니다.");
        if(taskEntity.getUserEntity().getUserId() != userId && studyMemberEntity.getRole() != StudyMemberRole.ROLE_OWNER && studyMemberEntity.getRole() != StudyMemberRole.ROLE_MANAGER)
            throw new IllegalArgumentException("과제는 등록한 사람 혹은 스터디 관리자만 수정할 수 있습니다");

        taskEntity.update(taskUpdateRequest.getTitle(), taskUpdateRequest.getContent(), taskUpdateRequest.getDueDate());
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다.(task_id 잘못됨)"));
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskEntity.getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 삭제하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("과제를 삭제하려는 스터디에 참여하고 있지 않습니다.");
        if(taskEntity.getUserEntity().getUserId() != userId && studyMemberEntity.getRole() != StudyMemberRole.ROLE_OWNER && studyMemberEntity.getRole() != StudyMemberRole.ROLE_MANAGER)
            throw new IllegalArgumentException("과제는 등록한 사람 혹은 스터디 관리자만 삭제할 수 있습니다");

        taskSubmissionRepository.findAllByTaskEntity_TaskId(taskId)
                .forEach(this::deleteSubmissionAttachments);
        taskSubmissionRepository.deleteAllByTaskEntity_TaskId(taskId);
        taskRepository.deleteById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponse> getTaskList(Long userId, Long studyId) {
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(studyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 조회하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("과제를 조회하려는 스터디에 참여하고 있지 않습니다.");

        List<TaskListResponse> list = taskRepository.findAllByStudyGroupEntity_StudyId(studyId)
                .stream()
                .map(TaskListResponse::from)
                .toList();

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDetailResponse getTaskDetail(Long userId, Long taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다.(task_id 잘못됨)"));
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskEntity.getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 조회하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("과제를 조회하려는 스터디에 참여하고 있지 않습니다.");

        return TaskDetailResponse.from(taskEntity);
    }

    @Override
    @Transactional
    public void submitTask(Long userId, Long taskId, TaskSubmitRequest taskSubmitRequest, List<MultipartFile> files) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다.(task_id 잘못됨)"));
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskEntity.getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("과제를 제출하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("과제를 제출하려는 스터디에 참여하고 있지 않습니다.");

        TaskSubmissionEntity taskSubmissionEntity = TaskSubmissionEntity
                .builder()
                .title(taskSubmitRequest.getTitle())
                .content(taskSubmitRequest.getContent())
                .taskEntity(taskEntity)
                .userEntity(studyMemberEntity.getUserEntity())
                .build();

        taskSubmissionRepository.save(taskSubmissionEntity);
        saveSubmissionAttachments(taskSubmissionEntity, files);

        TaskAiFeedbackEntity feedback = TaskAiFeedbackEntity.builder()
                .taskSubmissionEntity(taskSubmissionEntity)
                .build();
        taskAiFeedbackRepository.save(feedback);

        aiFeedbackOutboxService.enqueue(new AiFeedbackMessage(
                feedback.getId(),
                taskSubmissionEntity.getSubmissionId(),
                taskEntity.getTitle(),
                taskEntity.getContent(),
                taskSubmitRequest.getTitle(),
                taskSubmitRequest.getContent(),
                getSubmissionAttachmentUrls(taskSubmissionEntity.getSubmissionId())
        ));
    }

    @Override
    @Transactional
    public void modifySubmitTask(Long userId, Long submitId, TaskSubmitRequest taskSubmitRequest, List<MultipartFile> files) {
        TaskSubmissionEntity taskSubmissionEntity = taskSubmissionRepository.findById(submitId)
                .orElseThrow(() -> new IllegalArgumentException("submitId를 찾을 수 없습니다."));
        if(!taskSubmissionEntity.getUserEntity().getUserId().equals(userId))
            throw new IllegalArgumentException("본인의 과제만 수정할 수 있습니다.");
        taskSubmissionEntity.update(taskSubmitRequest.getTitle(), taskSubmitRequest.getContent());
        replaceSubmissionAttachments(taskSubmissionEntity, files);

        TaskAiFeedbackEntity taskAiFeedbackEntity = taskAiFeedbackRepository.findByTaskSubmissionEntity_SubmissionId(submitId)
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 상태입니다."));
        taskAiFeedbackEntity.cleanUp();
        aiFeedbackOutboxService.enqueue(new AiFeedbackMessage(
                taskAiFeedbackEntity.getId(),
                taskSubmissionEntity.getSubmissionId(),
                taskSubmissionEntity.getTaskEntity().getTitle(),
                taskSubmissionEntity.getTaskEntity().getContent(),
                taskSubmitRequest.getTitle(),
                taskSubmitRequest.getContent(),
                getSubmissionAttachmentUrls(taskSubmissionEntity.getSubmissionId())
        ));
    }

    @Override
    public List<TaskSubmissionListResponse> getSubmissionList(Long userId, Long taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다.(task_id 잘못됨)"));
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskEntity.getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("제출 목록을 조회하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("제출 목록을 조회하려는 스터디에 참여하고 있지 않습니다.");

        List<TaskSubmissionListResponse> list = taskSubmissionRepository.findAllByTaskEntity_TaskId(taskId)
                .stream()
                .map(TaskSubmissionListResponse::from)
                .toList();

        return list;
    }

    @Override
    public TaskSubmissionDetailResponse getSubmissionDetail(Long userId, Long submissionId) {
        TaskSubmissionEntity taskSubmissionEntity = taskSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다.(submission_id 잘못됨)"));
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(taskSubmissionEntity.getTaskEntity().getStudyGroupEntity().getStudyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("제출을 조회하려는 스터디에 참여하고 있지 않습니다."));
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_ACTIVE)
            throw new IllegalArgumentException("제출을 조회하려는 스터디에 참여하고 있지 않습니다.");

        return TaskSubmissionDetailResponse.from(
                taskSubmissionEntity,
                taskSubmissionAttachmentRepository.findAllByTaskSubmissionEntity_SubmissionId(submissionId)
        );
    }

    private void saveSubmissionAttachments(TaskSubmissionEntity submissionEntity, List<MultipartFile> files) {
        List<FileUploadResult> uploadResults = s3FileUploadService.uploadAll(
                files,
                "task-submissions/" + submissionEntity.getSubmissionId()
        );

        if (uploadResults.isEmpty()) {
            return;
        }

        List<TaskSubmissionAttachmentEntity> attachmentEntities = uploadResults.stream()
                .map(result -> TaskSubmissionAttachmentEntity.builder()
                        .taskSubmissionEntity(submissionEntity)
                        .originalFileName(result.getOriginalFileName())
                        .storedFileName(result.getStoredFileName())
                        .contentType(result.getContentType())
                        .fileKey(result.getFileKey())
                        .fileUrl(result.getFileUrl())
                        .fileSize(result.getFileSize())
                        .build())
                .toList();

        taskSubmissionAttachmentRepository.saveAll(attachmentEntities);
    }

    private void replaceSubmissionAttachments(TaskSubmissionEntity submissionEntity, List<MultipartFile> files) {
        if (!hasUploadableFiles(files)) {
            return;
        }

        deleteSubmissionAttachments(submissionEntity);
        saveSubmissionAttachments(submissionEntity, files);
    }

    private void deleteSubmissionAttachments(TaskSubmissionEntity submissionEntity) {
        List<TaskSubmissionAttachmentEntity> oldAttachments =
                taskSubmissionAttachmentRepository.findAllByTaskSubmissionEntity_SubmissionId(submissionEntity.getSubmissionId());

        if (oldAttachments.isEmpty()) {
            return;
        }

        s3FileUploadService.deleteAll(oldAttachments.stream()
                .map(TaskSubmissionAttachmentEntity::getFileKey)
                .toList());
        taskSubmissionAttachmentRepository.deleteAll(oldAttachments);
    }

    private boolean hasUploadableFiles(List<MultipartFile> files) {
        return files != null && files.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    private List<String> getSubmissionAttachmentUrls(Long submissionId) {
        return taskSubmissionAttachmentRepository.findAllByTaskSubmissionEntity_SubmissionId(submissionId)
                .stream()
                .map(TaskSubmissionAttachmentEntity::getFileUrl)
                .toList();
    }
}
