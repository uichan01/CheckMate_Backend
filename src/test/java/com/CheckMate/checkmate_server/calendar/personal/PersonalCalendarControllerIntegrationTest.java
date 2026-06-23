package com.CheckMate.checkmate_server.calendar.personal;

import com.CheckMate.checkmate_server.calendar.personal.domain.UserScheduleEntity;
import com.CheckMate.checkmate_server.calendar.personal.repository.PersonalCalendarRepository;
import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
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
class PersonalCalendarControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JWTUtil jwtUtil;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Autowired UserRepository userRepository;
    @Autowired PersonalCalendarRepository personalCalendarRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserEntity ownerUser;
    private UserEntity otherUser;
    private UserScheduleEntity schedule;

    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        ownerUser = userRepository.save(new UserEntity("owner@test.com", passwordEncoder.encode("pw"), "", "Owner", UserRole.ROLE_USER));
        otherUser = userRepository.save(new UserEntity("other@test.com", passwordEncoder.encode("pw"), "", "Other", UserRole.ROLE_USER));

        schedule = personalCalendarRepository.save(UserScheduleEntity.builder()
                .userEntity(ownerUser)
                .startTime(LocalDateTime.of(2026, 12, 31, 10, 0, 0))
                .title("내 일정")
                .content("일정 내용")
                .build());

        ownerToken = "Bearer " + jwtUtil.createJwt(ownerUser.getUserId(), "owner@test.com", "ROLE_USER", 3_600_000L);
        otherToken = "Bearer " + jwtUtil.createJwt(otherUser.getUserId(), "other@test.com", "ROLE_USER", 3_600_000L);
    }

    // ─── 개인 일정 생성 ───

    @Test
    @DisplayName("사용자는 개인 일정을 생성할 수 있다")
    void createPersonalCalendar_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "새 일정",
                "content", "일정 내용",
                "startTime", "2026-12-31T10:00:00",
                "place", "서울"
        ));

        mockMvc.perform(post("/api/v1/calendar/personal")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("제목이 없으면 개인 일정을 생성할 수 없다")
    void createPersonalCalendar_fail_blankTitle() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "",
                "content", "일정 내용",
                "startTime", "2026-12-31T10:00:00"
        ));

        mockMvc.perform(post("/api/v1/calendar/personal")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("시작 시간이 과거이면 개인 일정을 생성할 수 없다")
    void createPersonalCalendar_fail_pastStartTime() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "과거 일정",
                "content", "일정 내용",
                "startTime", "2020-01-01T10:00:00"
        ));

        mockMvc.perform(post("/api/v1/calendar/personal")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 개인 일정 수정 ───

    @Test
    @DisplayName("일정 소유자는 개인 일정을 수정할 수 있다")
    void updatePersonalCalendar_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "수정된 일정",
                "content", "수정된 내용",
                "startTime", "2026-12-31T12:00:00",
                "place", "부산"
        ));

        mockMvc.perform(patch("/api/v1/calendar/personal/{personal_calendar_id}", schedule.getUserScheduleId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일정 소유자가 아닌 사용자는 개인 일정을 수정할 수 없다")
    void updatePersonalCalendar_fail_notOwner() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "수정 시도",
                "content", "수정 내용",
                "startTime", "2026-12-31T12:00:00"
        ));

        mockMvc.perform(patch("/api/v1/calendar/personal/{personal_calendar_id}", schedule.getUserScheduleId())
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 개인 일정 삭제 ───

    @Test
    @DisplayName("일정 소유자는 개인 일정을 삭제할 수 있다")
    void deletePersonalCalendar_success() throws Exception {
        mockMvc.perform(delete("/api/v1/calendar/personal/{personal_calendar_id}", schedule.getUserScheduleId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일정 소유자가 아닌 사용자는 개인 일정을 삭제할 수 없다")
    void deletePersonalCalendar_fail_notOwner() throws Exception {
        mockMvc.perform(delete("/api/v1/calendar/personal/{personal_calendar_id}", schedule.getUserScheduleId())
                        .header("Authorization", otherToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 개인 일정 목록 조회 ───

    @Test
    @DisplayName("사용자는 본인의 개인 일정 목록을 조회할 수 있다")
    void getPersonalCalendarList_success() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/personal/list")
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("내 일정"));
    }

    @Test
    @DisplayName("일정이 없는 사용자의 목록은 비어 있다")
    void getPersonalCalendarList_empty() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/personal/list")
                        .header("Authorization", otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ─── 개인 일정 상세 조회 ───

    @Test
    @DisplayName("일정 소유자는 개인 일정 상세 정보를 조회할 수 있다")
    void getPersonalCalendarDetail_success() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/personal/{personal_calendar_id}", schedule.getUserScheduleId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.personalCalendarId").value(schedule.getUserScheduleId()))
                .andExpect(jsonPath("$.data.title").value("내 일정"))
                .andExpect(jsonPath("$.data.content").value("일정 내용"));
    }

    @Test
    @DisplayName("일정 소유자가 아닌 사용자는 개인 일정 상세 정보를 조회할 수 없다")
    void getPersonalCalendarDetail_fail_notOwner() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/personal/{personal_calendar_id}", schedule.getUserScheduleId())
                        .header("Authorization", otherToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 통합 시나리오 ───

    @Test
    @DisplayName("개인 일정을 생성하면 목록 조회에서 확인된다")
    void create_then_queryList_integration() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "통합 일정",
                "content", "통합 내용",
                "startTime", "2026-12-31T15:00:00"
        ));

        // Step 1: 다른 사용자가 일정 생성
        mockMvc.perform(post("/api/v1/calendar/personal")
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 2: 목록 조회 → 1건 확인
        mockMvc.perform(get("/api/v1/calendar/personal/list")
                        .header("Authorization", otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("통합 일정"));
    }
}
