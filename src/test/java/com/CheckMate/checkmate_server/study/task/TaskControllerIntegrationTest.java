package com.CheckMate.checkmate_server.study.task;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import com.CheckMate.checkmate_server.study.group.domain.*;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import com.CheckMate.checkmate_server.study.task.domain.TaskSubmissionEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.domain.UserRole;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JWTUtil jwtUtil;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Autowired UserRepository userRepository;
    @Autowired StudyCategoryRepository studyCategoryRepository;
    @Autowired StudyGroupRepository studyGroupRepository;
    @Autowired StudyMemberRepository studyMemberRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired TaskSubmissionRepository taskSubmissionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity pendingUser;
    private UserEntity outsiderUser;
    private StudyGroupEntity studyGroup;
    private TaskEntity task;

    private String ownerToken;
    private String memberToken;
    private String pendingToken;
    private String outsiderToken;

    @BeforeEach
    void setUp() {
        ownerUser    = userRepository.save(new UserEntity("owner@test.com",    passwordEncoder.encode("pw"), "", "Owner",    UserRole.ROLE_USER));
        memberUser   = userRepository.save(new UserEntity("member@test.com",   passwordEncoder.encode("pw"), "", "Member",   UserRole.ROLE_USER));
        pendingUser  = userRepository.save(new UserEntity("pending@test.com",  passwordEncoder.encode("pw"), "", "Pending",  UserRole.ROLE_USER));
        outsiderUser = userRepository.save(new UserEntity("outsider@test.com", passwordEncoder.encode("pw"), "", "Outsider", UserRole.ROLE_USER));

        StudyCategoryEntity category = studyCategoryRepository.save(new StudyCategoryEntity(null, "테스트 카테고리"));
        studyGroup = studyGroupRepository.save(
                new StudyGroupEntity(category, "테스트 스터디", "설명", GroupScope.SCOPE_PUBLIC, GroupJoinPolicy.JOIN_POLICY_INSTANT));

        studyMemberRepository.save(new StudyMemberEntity(studyGroup, ownerUser,   StudyMemberRole.ROLE_OWNER,  StudyMemberStatus.STATUS_ACTIVE));
        studyMemberRepository.save(new StudyMemberEntity(studyGroup, memberUser,  StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_ACTIVE));
        studyMemberRepository.save(new StudyMemberEntity(studyGroup, pendingUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        task = taskRepository.save(TaskEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(ownerUser)
                .title("테스트 과제")
                .content("과제 내용")
                .dueDate(LocalDateTime.now().plusDays(7))
                .build());

        ownerToken   = "Bearer " + jwtUtil.createJwt(ownerUser.getUserId(),   "owner@test.com",   "ROLE_USER", 3_600_000L);
        memberToken  = "Bearer " + jwtUtil.createJwt(memberUser.getUserId(),  "member@test.com",  "ROLE_USER", 3_600_000L);
        pendingToken = "Bearer " + jwtUtil.createJwt(pendingUser.getUserId(), "pending@test.com", "ROLE_USER", 3_600_000L);
        outsiderToken= "Bearer " + jwtUtil.createJwt(outsiderUser.getUserId(),"outsider@test.com","ROLE_USER", 3_600_000L);
    }

    // ─── 과제 생성 ───

    @Test
    @DisplayName("스터디 OWNER는 과제를 생성할 수 있다")
    void createTask_success_owner() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "title",   "새 과제",
                "content", "과제 내용",
                "dueDate", "2026-12-31T23:59:59"
        ));

        mockMvc.perform(post("/api/v1/study/task")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버(ROLE_MEMBER)는 과제를 생성할 수 없다")
    void createTask_fail_member() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "title",   "새 과제",
                "content", "과제 내용",
                "dueDate", "2026-12-31T23:59:59"
        ));

        mockMvc.perform(post("/api/v1/study/task")
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("가입 승인 대기 중인 멤버는 과제를 생성할 수 없다")
    void createTask_fail_pending() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "title",   "새 과제",
                "content", "과제 내용",
                "dueDate", "2026-12-31T23:59:59"
        ));

        mockMvc.perform(post("/api/v1/study/task")
                        .header("Authorization", pendingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("스터디 비멤버는 과제를 생성할 수 없다")
    void createTask_fail_outsider() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "title",   "새 과제",
                "content", "과제 내용",
                "dueDate", "2026-12-31T23:59:59"
        ));

        mockMvc.perform(post("/api/v1/study/task")
                        .header("Authorization", outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 과제 수정 ───

    @Test
    @DisplayName("과제 작성자는 과제를 수정할 수 있다")
    void updateTask_success_owner() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "taskId",  task.getTaskId(),
                "title",   "수정된 과제 제목",
                "content", "수정된 내용",
                "dueDate", "2026-11-30T23:59:59"
        ));

        mockMvc.perform(patch("/api/v1/study/task")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("과제 작성자가 아닌 일반 멤버는 과제를 수정할 수 없다")
    void updateTask_fail_member() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "taskId",  task.getTaskId(),
                "title",   "수정 시도",
                "content", "수정 내용",
                "dueDate", "2026-11-30T23:59:59"
        ));

        mockMvc.perform(patch("/api/v1/study/task")
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 과제 삭제 ───

    @Test
    @DisplayName("과제 작성자는 과제를 삭제할 수 있다")
    void deleteTask_success_owner() throws Exception {
        mockMvc.perform(delete("/api/v1/study/task/{taskId}", task.getTaskId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("과제 작성자가 아닌 일반 멤버는 과제를 삭제할 수 없다")
    void deleteTask_fail_member() throws Exception {
        mockMvc.perform(delete("/api/v1/study/task/{taskId}", task.getTaskId())
                        .header("Authorization", memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 과제 목록 조회 ───

    @Test
    @DisplayName("활성 멤버는 스터디의 과제 목록을 조회할 수 있다")
    void getTaskList_success() throws Exception {
        mockMvc.perform(get("/api/v1/study/task/list/{studyId}", studyGroup.getStudyId())
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("가입 대기 중인 멤버는 과제 목록을 조회할 수 없다")
    void getTaskList_fail_pending() throws Exception {
        mockMvc.perform(get("/api/v1/study/task/list/{studyId}", studyGroup.getStudyId())
                        .header("Authorization", pendingToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("스터디 비멤버는 과제 목록을 조회할 수 없다")
    void getTaskList_fail_outsider() throws Exception {
        mockMvc.perform(get("/api/v1/study/task/list/{studyId}", studyGroup.getStudyId())
                        .header("Authorization", outsiderToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 과제 상세 조회 ───

    @Test
    @DisplayName("활성 멤버는 과제 상세 정보를 조회할 수 있다")
    void getTaskDetail_success() throws Exception {
        mockMvc.perform(get("/api/v1/study/task/{taskId}", task.getTaskId())
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(task.getTaskId()))
                .andExpect(jsonPath("$.data.title").value("테스트 과제"));
    }

    @Test
    @DisplayName("가입 대기 중인 멤버는 과제 상세 정보를 조회할 수 없다")
    void getTaskDetail_fail_pending() throws Exception {
        mockMvc.perform(get("/api/v1/study/task/{taskId}", task.getTaskId())
                        .header("Authorization", pendingToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 과제 제출 ───

    @Test
    @DisplayName("활성 멤버는 과제를 제출할 수 있다")
    void submitTask_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title",   "제출 제목",
                "content", "제출 내용"
        ));

        mockMvc.perform(post("/api/v1/study/task/{taskId}/submit", task.getTaskId())
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("가입 대기 중인 멤버는 과제를 제출할 수 없다")
    void submitTask_fail_pending() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title",   "제출 제목",
                "content", "제출 내용"
        ));

        mockMvc.perform(post("/api/v1/study/task/{taskId}/submit", task.getTaskId())
                        .header("Authorization", pendingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 제출 목록 조회 ───

    @Test
    @DisplayName("활성 멤버는 과제의 제출 목록을 조회할 수 있다")
    void getSubmissionList_success() throws Exception {
        taskSubmissionRepository.save(TaskSubmissionEntity.builder()
                .title("제출 제목")
                .content("제출 내용")
                .taskEntity(task)
                .userEntity(memberUser)
                .build());

        mockMvc.perform(get("/api/v1/study/task/submission/{taskId}/list", task.getTaskId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("가입 대기 중인 멤버는 제출 목록을 조회할 수 없다")
    void getSubmissionList_fail_pending() throws Exception {
        mockMvc.perform(get("/api/v1/study/task/submission/{taskId}/list", task.getTaskId())
                        .header("Authorization", pendingToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 제출 상세 조회 ───

    @Test
    @DisplayName("활성 멤버는 과제 제출 상세 정보를 조회할 수 있다")
    void getSubmissionDetail_success() throws Exception {
        TaskSubmissionEntity submission = taskSubmissionRepository.save(TaskSubmissionEntity.builder()
                .title("제출 제목")
                .content("제출 내용")
                .taskEntity(task)
                .userEntity(memberUser)
                .build());

        mockMvc.perform(get("/api/v1/study/task/submission/{submissionId}", submission.getSubmissionId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.submissionId").value(submission.getSubmissionId()))
                .andExpect(jsonPath("$.data.title").value("제출 제목"))
                .andExpect(jsonPath("$.data.nickname").value("Member"));
    }

    @Test
    @DisplayName("가입 대기 중인 멤버는 제출 상세 정보를 조회할 수 없다")
    void getSubmissionDetail_fail_pending() throws Exception {
        TaskSubmissionEntity submission = taskSubmissionRepository.save(TaskSubmissionEntity.builder()
                .title("제출 제목")
                .content("제출 내용")
                .taskEntity(task)
                .userEntity(memberUser)
                .build());

        mockMvc.perform(get("/api/v1/study/task/submission/{submissionId}", submission.getSubmissionId())
                        .header("Authorization", pendingToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 통합 시나리오 ───

    @Test
    @DisplayName("멤버가 과제를 제출하면 제출 목록과 상세 조회에서 확인된다")
    void submit_then_querySubmission_integration() throws Exception {
        String submitBody = objectMapper.writeValueAsString(Map.of(
                "title",   "통합 제출",
                "content", "통합 내용"
        ));

        // Step 1: 멤버가 과제 제출
        mockMvc.perform(post("/api/v1/study/task/{taskId}/submit", task.getTaskId())
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 2: OWNER가 제출 목록 조회 → 1건 확인
        mockMvc.perform(get("/api/v1/study/task/submission/{taskId}/list", task.getTaskId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].nickname").value("Member"));
    }
}
