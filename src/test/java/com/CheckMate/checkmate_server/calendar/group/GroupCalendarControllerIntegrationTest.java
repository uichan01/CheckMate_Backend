package com.CheckMate.checkmate_server.calendar.group;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import com.CheckMate.checkmate_server.study.group.domain.*;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GroupCalendarControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JWTUtil jwtUtil;
    @Autowired org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @Autowired UserRepository userRepository;
    @Autowired StudyCategoryRepository studyCategoryRepository;
    @Autowired StudyGroupRepository studyGroupRepository;
    @Autowired StudyMemberRepository studyMemberRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired MeetingRepository meetingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserEntity ownerUser;
    private StudyGroupEntity studyGroup;

    private String ownerToken;

    @BeforeEach
    void setUp() {
        ownerUser = userRepository.save(new UserEntity("owner@test.com", passwordEncoder.encode("pw"), "", "Owner", UserRole.ROLE_USER));

        StudyCategoryEntity category = studyCategoryRepository.save(new StudyCategoryEntity(null, "테스트 카테고리"));
        studyGroup = studyGroupRepository.save(
                new StudyGroupEntity(category, "테스트 스터디", "설명", GroupScope.SCOPE_PUBLIC, GroupJoinPolicy.JOIN_POLICY_INSTANT));

        studyMemberRepository.save(new StudyMemberEntity(studyGroup, ownerUser, StudyMemberRole.ROLE_OWNER, StudyMemberStatus.STATUS_ACTIVE));

        // 조회 범위(2026-07) 안의 과제
        taskRepository.save(TaskEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(ownerUser)
                .title("범위 내 과제")
                .content("과제 내용")
                .dueDate(LocalDateTime.of(2026, 7, 15, 23, 59, 0))
                .build());

        // 조회 범위 밖(2026-08)의 과제 → 결과에 포함되면 안 됨
        taskRepository.save(TaskEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(ownerUser)
                .title("범위 밖 과제")
                .content("과제 내용")
                .dueDate(LocalDateTime.of(2026, 8, 10, 23, 59, 0))
                .build());

        // 조회 범위 안의 미팅
        meetingRepository.save(MeetingEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(ownerUser)
                .title("범위 내 미팅")
                .content("미팅 내용")
                .meetingDate(LocalDateTime.of(2026, 7, 20, 10, 0, 0))
                .meetingPlace("서울")
                .build());

        ownerToken = "Bearer " + jwtUtil.createJwt(ownerUser.getUserId(), "owner@test.com", "ROLE_USER", 3_600_000L);
    }

    @Test
    @DisplayName("조회 기간 내의 과제와 미팅이 그룹 일정으로 조회된다")
    void getGroupSchedules_success() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/group/{studyId}/schedule", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .param("start_date", "2026-07-01")
                        .param("end_date", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("그룹 일정은 날짜 오름차순으로 정렬되어 조회된다")
    void getGroupSchedules_sortedByDate() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/group/{studyId}/schedule", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .param("start_date", "2026-07-01")
                        .param("end_date", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // 과제(7/15)가 미팅(7/20)보다 먼저 온다
                .andExpect(jsonPath("$.data[0].scheduleType").value("TASK"))
                .andExpect(jsonPath("$.data[0].title").value("범위 내 과제"))
                .andExpect(jsonPath("$.data[1].scheduleType").value("MEETING"))
                .andExpect(jsonPath("$.data[1].title").value("범위 내 미팅"));
    }

    @Test
    @DisplayName("조회 기간 밖의 일정은 조회되지 않는다")
    void getGroupSchedules_excludeOutOfRange() throws Exception {
        // 8월만 조회 → 범위 밖 과제 1건만 존재
        mockMvc.perform(get("/api/v1/calendar/group/{studyId}/schedule", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .param("start_date", "2026-08-01")
                        .param("end_date", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("범위 밖 과제"));
    }

    @Test
    @DisplayName("일정이 없는 기간을 조회하면 빈 목록이 반환된다")
    void getGroupSchedules_empty() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/group/{studyId}/schedule", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .param("start_date", "2026-09-01")
                        .param("end_date", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("인증 토큰이 없으면 그룹 일정을 조회할 수 없다")
    void getGroupSchedules_fail_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/group/{studyId}/schedule", studyGroup.getStudyId())
                        .param("start_date", "2026-07-01")
                        .param("end_date", "2026-07-31"))
                .andExpect(status().is4xxClientError());
    }
}
