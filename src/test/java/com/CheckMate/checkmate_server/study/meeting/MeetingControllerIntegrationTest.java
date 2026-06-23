package com.CheckMate.checkmate_server.study.meeting;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import com.CheckMate.checkmate_server.study.group.domain.*;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingParticipantRepository;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
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
class MeetingControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JWTUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Autowired UserRepository userRepository;
    @Autowired StudyCategoryRepository studyCategoryRepository;
    @Autowired StudyGroupRepository studyGroupRepository;
    @Autowired StudyMemberRepository studyMemberRepository;
    @Autowired MeetingRepository meetingRepository;
    @Autowired MeetingParticipantRepository meetingParticipantRepository;

    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity pendingUser;
    private UserEntity outsiderUser;
    private StudyGroupEntity studyGroup;
    private MeetingEntity futureMeeting;   // 미래 시각 (참여용)
    private MeetingEntity pastMeeting;     // 과거 시각 (출석용)

    private String ownerToken;
    private String memberToken;
    private String pendingToken;
    private String outsiderToken;

    @BeforeEach
    void setUp() {
        ownerUser   = userRepository.save(new UserEntity("owner@test.com",   passwordEncoder.encode("pw"), "", "Owner",   UserRole.ROLE_USER));
        memberUser  = userRepository.save(new UserEntity("member@test.com",  passwordEncoder.encode("pw"), "", "Member",  UserRole.ROLE_USER));
        pendingUser = userRepository.save(new UserEntity("pending@test.com", passwordEncoder.encode("pw"), "", "Pending", UserRole.ROLE_USER));
        outsiderUser= userRepository.save(new UserEntity("outsider@test.com",passwordEncoder.encode("pw"), "", "Outsider",UserRole.ROLE_USER));

        StudyCategoryEntity category = studyCategoryRepository.save(new StudyCategoryEntity(null, "테스트 카테고리"));
        studyGroup = studyGroupRepository.save(
                new StudyGroupEntity(category, "테스트 스터디", "설명", GroupScope.SCOPE_PUBLIC, GroupJoinPolicy.JOIN_POLICY_INSTANT));

        studyMemberRepository.save(new StudyMemberEntity(studyGroup, ownerUser,   StudyMemberRole.ROLE_OWNER,  StudyMemberStatus.STATUS_ACTIVE));
        studyMemberRepository.save(new StudyMemberEntity(studyGroup, memberUser,  StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_ACTIVE));
        studyMemberRepository.save(new StudyMemberEntity(studyGroup, pendingUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        futureMeeting = meetingRepository.save(MeetingEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(ownerUser)
                .title("미래 미팅")
                .content("내용")
                .meetingDate(LocalDateTime.now().plusDays(1))
                .meetingPlace("서울 강남")
                .build());

        // 출석 테스트용: 시작 시각이 과거이고 10분 이내
        pastMeeting = meetingRepository.save(MeetingEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(ownerUser)
                .title("과거 미팅")
                .content("내용")
                .meetingDate(LocalDateTime.now().minusMinutes(5))
                .meetingPlace("서울 종로")
                .build());

        ownerToken   = "Bearer " + jwtUtil.createJwt("owner@test.com",   "ROLE_USER", 3_600_000L);
        memberToken  = "Bearer " + jwtUtil.createJwt("member@test.com",  "ROLE_USER", 3_600_000L);
        pendingToken = "Bearer " + jwtUtil.createJwt("pending@test.com", "ROLE_USER", 3_600_000L);
        outsiderToken= "Bearer " + jwtUtil.createJwt("outsider@test.com","ROLE_USER", 3_600_000L);
    }

    // 1. 미팅 생성 성공
    @Test
    @DisplayName("스터디 활성 멤버는 미팅을 생성할 수 있다")
    void createMeeting_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "meetingTime", "2026-07-01T10:00:00",
                "title", "신규 미팅",
                "content", "미팅 내용",
                "meetingPlace", "부산"
        ));

        mockMvc.perform(post("/api/v1/study/meeting")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    // 2. 미팅 생성 실패 — PENDING 멤버
    @Test
    @DisplayName("가입 승인 대기 중인 멤버는 미팅을 생성할 수 없다")
    void createMeeting_fail_pendingMember() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "meetingTime", "2026-07-01T10:00:00",
                "title", "신규 미팅",
                "content", "미팅 내용",
                "meetingPlace", "부산"
        ));

        mockMvc.perform(post("/api/v1/study/meeting")
                        .header("Authorization", pendingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // 3. 미팅 생성 실패 — 스터디 비멤버
    @Test
    @DisplayName("스터디 멤버가 아닌 사용자는 미팅을 생성할 수 없다")
    void createMeeting_fail_notMember() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studyId", studyGroup.getStudyId(),
                "meetingTime", "2026-07-01T10:00:00",
                "title", "신규 미팅",
                "content", "미팅 내용",
                "meetingPlace", "부산"
        ));

        mockMvc.perform(post("/api/v1/study/meeting")
                        .header("Authorization", outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // 4. 미팅 수정 성공 — 생성자
    @Test
    @DisplayName("미팅 생성자는 미팅을 수정할 수 있다")
    void updateMeeting_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "meetingId", futureMeeting.getMeetingId(),
                "meetingTime", "2026-08-01T14:00:00",
                "title", "수정된 미팅",
                "content", "수정된 내용",
                "meetingPlace", "인천"
        ));

        mockMvc.perform(patch("/api/v1/study/meeting")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(futureMeeting.getMeetingId()));
    }

    // 5. 미팅 수정 실패 — 생성자가 아닌 멤버
    @Test
    @DisplayName("미팅 생성자가 아닌 멤버는 미팅을 수정할 수 없다")
    void updateMeeting_fail_notCreator() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "meetingId", futureMeeting.getMeetingId(),
                "meetingTime", "2026-08-01T14:00:00",
                "title", "수정 시도",
                "content", "수정 내용",
                "meetingPlace", "인천"
        ));

        mockMvc.perform(patch("/api/v1/study/meeting")
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // 6. 미팅 삭제 성공 — 생성자
    @Test
    @DisplayName("미팅 생성자는 미팅을 삭제할 수 있다")
    void deleteMeeting_success() throws Exception {
        long meetingId = futureMeeting.getMeetingId();

        mockMvc.perform(delete("/api/v1/study/meeting/{meeting_id}", meetingId)
                        .param("meeting_id", String.valueOf(meetingId))
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 7. 미팅 삭제 실패 — 생성자가 아닌 멤버
    @Test
    @DisplayName("미팅 생성자가 아닌 멤버는 미팅을 삭제할 수 없다")
    void deleteMeeting_fail_notCreator() throws Exception {
        long meetingId = futureMeeting.getMeetingId();

        mockMvc.perform(delete("/api/v1/study/meeting/{meeting_id}", meetingId)
                        .param("meeting_id", String.valueOf(meetingId))
                        .header("Authorization", memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // 8. 미팅 목록 조회 성공
    @Test
    @DisplayName("활성 멤버는 스터디의 미팅 목록을 조회할 수 있다")
    void getMeetingList_success() throws Exception {
        long studyId = studyGroup.getStudyId();

        mockMvc.perform(get("/api/v1/study/meeting/list/{study_id}", studyId)
                        .param("study_id", String.valueOf(studyId))
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // 9. 미팅 상세 조회 성공
    @Test
    @DisplayName("활성 멤버는 미팅 상세 정보를 조회할 수 있다")
    void getMeetingDetail_success() throws Exception {
        long meetingId = futureMeeting.getMeetingId();

        mockMvc.perform(get("/api/v1/study/meeting/detail/{meeting_id}", meetingId)
                        .param("meeting_id", String.valueOf(meetingId))
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingId").value(meetingId))
                .andExpect(jsonPath("$.data.title").value("미래 미팅"))
                .andExpect(jsonPath("$.data.creatorName").value("Owner"))
                .andExpect(jsonPath("$.data.participateUsers").isArray());
    }

    // 10. 미팅 참여 → 출석 체크 통합 시나리오
    @Test
    @DisplayName("멤버가 미팅에 참여 후 출석 시각 내에 출석 체크하면 PRESENT 상태가 된다")
    void participateMeeting_then_attendanceMeeting_success() throws Exception {
        long meetingId = pastMeeting.getMeetingId();

        // Step 1: 미팅 참여
        mockMvc.perform(post("/api/v1/study/meeting/participate/{meeting_id}", meetingId)
                        .param("meeting_id", String.valueOf(meetingId))
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 2: 출석 체크 (미팅 시작 후 10분 이내 → PRESENT)
        mockMvc.perform(post("/api/v1/study/meeting/attendance/{meeting_id}", meetingId)
                        .param("meeting_id", String.valueOf(meetingId))
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
