package com.CheckMate.checkmate_server.study.task.ai;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import com.CheckMate.checkmate_server.study.group.domain.*;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus;
import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionRepository;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.domain.UserRole;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDateTime;
import java.util.Map;

import com.CheckMate.checkmate_server.config.EmbeddedRedisConfig;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
// @Transactional 사용 금지 — Consumer가 다른 스레드에서 커밋된 데이터를 읽어야 하므로
class TaskAiFeedbackIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JWTUtil jwtUtil;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Autowired UserRepository userRepository;
    @Autowired StudyCategoryRepository studyCategoryRepository;
    @Autowired StudyGroupRepository studyGroupRepository;
    @Autowired StudyMemberRepository studyMemberRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired TaskSubmissionRepository submissionRepository;
    @Autowired TaskAiFeedbackRepository feedbackRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserEntity tester;
    private TaskEntity task;

    @BeforeEach
    void setUp() {
        tester = userRepository.save(new UserEntity(
                "ai_tester@test.com", passwordEncoder.encode("pw"), "", "AITester", UserRole.ROLE_USER));

        StudyCategoryEntity category = studyCategoryRepository.save(
                new StudyCategoryEntity(null, "AI 테스트 카테고리"));

        StudyGroupEntity studyGroup = studyGroupRepository.save(
                new StudyGroupEntity(category, "AI 테스트 스터디", "설명",
                        GroupScope.SCOPE_PUBLIC, GroupJoinPolicy.JOIN_POLICY_INSTANT));

        studyMemberRepository.save(new StudyMemberEntity(
                studyGroup, tester, StudyMemberRole.ROLE_OWNER, StudyMemberStatus.STATUS_ACTIVE));

        task = taskRepository.save(TaskEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(tester)
                .title("스프링 IoC 컨테이너 분석")
                .content("스프링 부트의 IoC 컨테이너에 대해 설명하고, 생성자 주입과 필드 주입의 차이점을 분석하시오.")
                .dueDate(LocalDateTime.now().plusDays(7))
                .build());
    }

    @AfterEach
    void tearDown() {
        // FK 순서에 맞춰 삭제
        feedbackRepository.deleteAll();
        submissionRepository.deleteAll();
        taskRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyGroupRepository.deleteAll();
        studyCategoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("과제 제출 시 AI 피드백이 비동기로 생성되고 API로 조회된다")
    void submitTask_thenAiFeedbackIsGeneratedAsynchronously() throws Exception {

        String token = "Bearer " + jwtUtil.createJwt(
                tester.getUserId(), "ai_tester@test.com", "ROLE_USER", 3_600_000L);

        String submitBody = objectMapper.writeValueAsString(Map.of(
                "title",   "IoC 컨테이너 분석 보고서",
                "content", "스프링의 IoC 컨테이너는 빈의 생명주기를 관리합니다. " +
                           "생성자 주입은 불변성과 테스트 용이성 측면에서 유리하고, " +
                           "필드 주입은 간결하지만 순환 참조 탐지가 어렵습니다. " +
                           "따라서 공식 가이드에서는 생성자 주입을 권장합니다."
        ));

        log.info("");
        log.info("══════════════════════════════════════════════════════════");
        log.info("  AI 피드백 통합 테스트 시작");
        log.info("══════════════════════════════════════════════════════════");

        // ── Step 1: 과제 제출 ──────────────────────────────────────────
        log.info("");
        log.info("▶ [1단계] 과제 제출");
        log.info("  과제 제목 : {}", task.getTitle());
        log.info("  제출 제목 : IoC 컨테이너 분석 보고서");

        mockMvc.perform(post("/api/v1/study/task/{taskId}/submit", task.getTaskId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        log.info("  ✔ 제출 완료 → task_submissions 저장, Redis 메시지 발행됨");

        // ── Step 2: 피드백 레코드 PENDING 확인 ────────────────────────
        Long submissionId = submissionRepository.findAll().stream()
                .filter(s -> s.getTaskEntity().getTaskId().equals(task.getTaskId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("제출 레코드를 찾을 수 없습니다"))
                .getSubmissionId();

        TaskAiFeedbackEntity initial = feedbackRepository
                .findByTaskSubmissionEntity_SubmissionId(submissionId)
                .orElseThrow(() -> new IllegalStateException("피드백 레코드가 생성되지 않았습니다"));

        log.info("");
        log.info("▶ [2단계] 초기 피드백 상태 확인");
        log.info("  feedbackId : {}", initial.getId());
        log.info("  status     : {}", initial.getStatus());
        log.info("  createdAt  : {}", initial.getCreatedAt());
        assertTrue(
            initial.getStatus() == AiFeedbackStatus.PENDING || initial.getStatus() == AiFeedbackStatus.PROCESSING,
            "초기 상태는 PENDING 또는 PROCESSING이어야 합니다 (embedded Redis는 즉시 처리됨)"
        );
        log.info("  ✔ 초기 상태 확인 완료 ({})", initial.getStatus());

        // ── Step 3: AI 생성 완료 대기 (최대 60초 폴링) ────────────────
        log.info("");
        log.info("▶ [3단계] AI 피드백 생성 대기 중 (최대 60초)...");

        TaskAiFeedbackEntity result = null;
        for (int elapsed = 2; elapsed <= 60; elapsed += 2) {
            Thread.sleep(2_000);
            result = feedbackRepository
                    .findByTaskSubmissionEntity_SubmissionId(submissionId)
                    .orElse(null);

            AiFeedbackStatus status = (result != null) ? result.getStatus() : AiFeedbackStatus.PENDING;
            log.info("  {}초 경과 → 현재 상태: {}", elapsed, status);

            if (status == AiFeedbackStatus.COMPLETED || status == AiFeedbackStatus.FAILED) {
                break;
            }
        }

        // ── Step 4: 결과 출력 및 검증 ────────────────────────────────
        log.info("");
        log.info("▶ [4단계] AI 피드백 결과");
        assertNotNull(result, "AI 피드백 결과가 없습니다 (타임아웃)");

        if (result.getStatus() == AiFeedbackStatus.COMPLETED) {
            log.info("  상태     : ✅ COMPLETED");
            log.info("  시작시각 : {}", result.getStartedAt());
            log.info("  완료시각 : {}", result.getCompletedAt());
            log.info("");
            log.info("  ┌─ 잘한 점  ────────────────────────────────────────");
            log.info("  │  {}", result.getStrength());
            log.info("  ├─ 부족한 점 ────────────────────────────────────────");
            log.info("  │  {}", result.getWeakness());
            log.info("  └─ 개선 제안 ────────────────────────────────────────");
            log.info("     {}", result.getSuggestion());

            assertNotNull(result.getStrength(),   "strength가 null입니다");
            assertNotNull(result.getWeakness(),   "weakness가 null입니다");
            assertNotNull(result.getSuggestion(), "suggestion이 null입니다");
        } else {
            log.error("  상태     : ❌ FAILED");
            log.error("  실패 원인 : {}", result.getErrorMessage());
            fail("AI 피드백 생성 실패: " + result.getErrorMessage());
        }

        // ── Step 5: API 조회 검증 ─────────────────────────────────────
        log.info("");
        log.info("▶ [5단계] GET /api/v1/study/task/ai-feedback/{} API 검증", submissionId);

        mockMvc.perform(get("/api/v1/study/task/ai-feedback/{submissionId}", submissionId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.strength").isNotEmpty())
                .andExpect(jsonPath("$.data.weakness").isNotEmpty())
                .andExpect(jsonPath("$.data.suggestion").isNotEmpty())
                .andDo(res -> log.info("  API 응답: {}", res.getResponse().getContentAsString()));

        log.info("");
        log.info("══════════════════════════════════════════════════════════");
        log.info("  AI 피드백 통합 테스트 완료 ✅");
        log.info("══════════════════════════════════════════════════════════");
        log.info("");
    }
}
