package com.CheckMate.checkmate_server.study.group;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import com.CheckMate.checkmate_server.study.group.domain.*;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.domain.UserRole;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudyGroupControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JWTUtil jwtUtil;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Autowired UserRepository userRepository;
    @Autowired StudyCategoryRepository studyCategoryRepository;
    @Autowired StudyGroupRepository studyGroupRepository;
    @Autowired StudyMemberRepository studyMemberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity outsiderUser;

    private StudyCategoryEntity category;
    private StudyGroupEntity studyGroup;
    private StudyGroupEntity approvalGroup;

    private String ownerToken;
    private String memberToken;
    private String outsiderToken;

    @BeforeEach
    void setUp() {
        ownerUser    = userRepository.save(new UserEntity("owner@test.com",    passwordEncoder.encode("pw"), "", "Owner",    UserRole.ROLE_USER));
        memberUser   = userRepository.save(new UserEntity("member@test.com",   passwordEncoder.encode("pw"), "", "Member",   UserRole.ROLE_USER));
        outsiderUser = userRepository.save(new UserEntity("outsider@test.com", passwordEncoder.encode("pw"), "", "Outsider", UserRole.ROLE_USER));

        category = studyCategoryRepository.save(new StudyCategoryEntity(null, "개발"));

        // 즉시 가입 공개 스터디
        studyGroup = studyGroupRepository.save(new StudyGroupEntity(
                category, "알고리즘 스터디", "설명", GroupScope.SCOPE_PUBLIC, GroupJoinPolicy.JOIN_POLICY_INSTANT));
        studyMemberRepository.save(new StudyMemberEntity(studyGroup, ownerUser,  StudyMemberRole.ROLE_OWNER,  StudyMemberStatus.STATUS_ACTIVE));
        studyMemberRepository.save(new StudyMemberEntity(studyGroup, memberUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_ACTIVE));

        // 승인 필요 공개 스터디
        approvalGroup = studyGroupRepository.save(new StudyGroupEntity(
                category, "승인 스터디", "설명", GroupScope.SCOPE_PUBLIC, GroupJoinPolicy.JOIN_POLICY_APPROVAL));
        studyMemberRepository.save(new StudyMemberEntity(approvalGroup, ownerUser, StudyMemberRole.ROLE_OWNER, StudyMemberStatus.STATUS_ACTIVE));

        ownerToken    = "Bearer " + jwtUtil.createJwt(ownerUser.getUserId(),    "owner@test.com",    "ROLE_USER", 3_600_000L);
        memberToken   = "Bearer " + jwtUtil.createJwt(memberUser.getUserId(),   "member@test.com",   "ROLE_USER", 3_600_000L);
        outsiderToken = "Bearer " + jwtUtil.createJwt(outsiderUser.getUserId(), "outsider@test.com", "ROLE_USER", 3_600_000L);
    }

    // ─── 스터디 그룹 생성 ───

    @Test
    @DisplayName("유효한 요청으로 스터디 그룹을 생성하면 생성된 studyId를 반환한다")
    void createStudyGroup_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "categoryId", category.getCategoryId(),
                "title", "새 스터디",
                "description", "설명",
                "scope", "SCOPE_PUBLIC",
                "joinPolicy", "JOIN_POLICY_INSTANT"
        ));

        mockMvc.perform(post("/api/v1/study/group")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 스터디 그룹을 생성하면 실패한다")
    void createStudyGroup_fail_invalidCategory() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "categoryId", 999999L,
                "title", "새 스터디",
                "description", "설명",
                "scope", "SCOPE_PUBLIC",
                "joinPolicy", "JOIN_POLICY_INSTANT"
        ));

        mockMvc.perform(post("/api/v1/study/group")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 그룹 목록 검색 ───

    @Test
    @DisplayName("조건 없이 검색하면 전체 스터디 목록을 반환한다")
    void searchStudyGroups_noFilter() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/list")
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("키워드로 검색하면 제목에 키워드가 포함된 스터디 목록을 반환한다")
    void searchStudyGroups_withKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/list")
                        .header("Authorization", ownerToken)
                        .param("keyword", "알고리즘"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("알고리즘 스터디"));
    }

    @Test
    @DisplayName("카테고리 ID로 검색하면 해당 카테고리의 스터디 목록을 반환한다")
    void searchStudyGroups_withCategory() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/list")
                        .header("Authorization", ownerToken)
                        .param("categoryId", String.valueOf(category.getCategoryId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ─── 스터디 그룹 상세 조회 ───

    @Test
    @DisplayName("공개 스터디는 비멤버도 상세 조회할 수 있다")
    void getStudyGroupDetail_success_public() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/{studyId}", studyGroup.getStudyId())
                        .header("Authorization", outsiderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("알고리즘 스터디"));
    }

    @Test
    @DisplayName("비공개 스터디는 활성 멤버만 상세 조회할 수 있다")
    void getStudyGroupDetail_success_private_member() throws Exception {
        StudyGroupEntity privateGroup = studyGroupRepository.save(new StudyGroupEntity(
                category, "비공개 스터디", "설명", GroupScope.SCOPE_PRIVATE, GroupJoinPolicy.JOIN_POLICY_INSTANT));
        studyMemberRepository.save(new StudyMemberEntity(privateGroup, ownerUser, StudyMemberRole.ROLE_OWNER, StudyMemberStatus.STATUS_ACTIVE));

        mockMvc.perform(get("/api/v1/study/group/{studyId}", privateGroup.getStudyId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비공개 스터디는 비멤버가 상세 조회하면 실패한다")
    void getStudyGroupDetail_fail_private_outsider() throws Exception {
        StudyGroupEntity privateGroup = studyGroupRepository.save(new StudyGroupEntity(
                category, "비공개 스터디", "설명", GroupScope.SCOPE_PRIVATE, GroupJoinPolicy.JOIN_POLICY_INSTANT));
        studyMemberRepository.save(new StudyMemberEntity(privateGroup, ownerUser, StudyMemberRole.ROLE_OWNER, StudyMemberStatus.STATUS_ACTIVE));

        mockMvc.perform(get("/api/v1/study/group/{studyId}", privateGroup.getStudyId())
                        .header("Authorization", outsiderToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 그룹 수정 ───

    @Test
    @DisplayName("OWNER는 스터디 그룹을 수정할 수 있다")
    void updateStudyGroup_success_owner() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "수정된 스터디"
        ));

        mockMvc.perform(patch("/api/v1/study/group/{studyId}", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버는 스터디 그룹을 수정할 수 없다")
    void updateStudyGroup_fail_member() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "수정 시도"
        ));

        mockMvc.perform(patch("/api/v1/study/group/{studyId}", studyGroup.getStudyId())
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 멤버 초대 ───

    @Test
    @DisplayName("OWNER는 외부 유저를 스터디에 초대할 수 있다")
    void addStudyMember_success_owner() throws Exception {
        mockMvc.perform(post("/api/v1/study/group/{studyId}/members", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"outsider@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버는 다른 유저를 스터디에 초대할 수 없다")
    void addStudyMember_fail_member() throws Exception {
        mockMvc.perform(post("/api/v1/study/group/{studyId}/members", studyGroup.getStudyId())
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"outsider@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 초대하면 실패한다")
    void addStudyMember_fail_unknownEmail() throws Exception {
        mockMvc.perform(post("/api/v1/study/group/{studyId}/members", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 멤버 제거 ───

    @Test
    @DisplayName("OWNER는 일반 멤버를 스터디에서 제거할 수 있다")
    void removeStudyMember_success_owner() throws Exception {
        mockMvc.perform(delete("/api/v1/study/group/{studyId}/members", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버는 다른 멤버를 제거할 수 없다")
    void removeStudyMember_fail_member() throws Exception {
        mockMvc.perform(delete("/api/v1/study/group/{studyId}/members", studyGroup.getStudyId())
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("OWNER는 자기 자신을 제거할 수 없다")
    void removeStudyMember_fail_selfRemove() throws Exception {
        mockMvc.perform(delete("/api/v1/study/group/{studyId}/members", studyGroup.getStudyId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 내 스터디 그룹 목록 ───

    @Test
    @DisplayName("자신이 속한 스터디 그룹 목록을 조회할 수 있다")
    void getMyStudyGroups_success() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/list/me")
                        .header("Authorization", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("스터디에 속하지 않은 유저의 내 스터디 목록은 비어있다")
    void getMyStudyGroups_empty_outsider() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/list/me")
                        .header("Authorization", outsiderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ─── 스터디원 역할 변경 ───

    @Test
    @DisplayName("OWNER는 일반 멤버를 MANAGER로 역할을 변경할 수 있다")
    void setStudyMemberRole_success_toManager() throws Exception {
        mockMvc.perform(patch("/api/v1/study/group/{studyId}/member_role/{role}",
                        studyGroup.getStudyId(), StudyMemberRole.ROLE_MANAGER)
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버는 다른 멤버의 역할을 변경할 수 없다")
    void setStudyMemberRole_fail_member() throws Exception {
        mockMvc.perform(patch("/api/v1/study/group/{studyId}/member_role/{role}",
                        studyGroup.getStudyId(), StudyMemberRole.ROLE_MANAGER)
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 그룹 신청 ───

    @Test
    @DisplayName("즉시 가입 스터디에 신청하면 STATUS_ACTIVE를 반환한다")
    void requestStudyGroup_success_instant() throws Exception {
        mockMvc.perform(post("/api/v1/study/group/{studyId}/request", studyGroup.getStudyId())
                        .header("Authorization", outsiderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("STATUS_ACTIVE"));
    }

    @Test
    @DisplayName("승인 필요 스터디에 신청하면 STATUS_PENDING을 반환한다")
    void requestStudyGroup_success_approval() throws Exception {
        mockMvc.perform(post("/api/v1/study/group/{studyId}/request", approvalGroup.getStudyId())
                        .header("Authorization", outsiderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("STATUS_PENDING"));
    }

    @Test
    @DisplayName("이미 가입된 스터디에 재신청하면 실패한다")
    void requestStudyGroup_fail_alreadyJoined() throws Exception {
        mockMvc.perform(post("/api/v1/study/group/{studyId}/request", studyGroup.getStudyId())
                        .header("Authorization", memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 신청 목록 조회 ───

    @Test
    @DisplayName("OWNER는 대기 중인 신청 목록을 조회할 수 있다")
    void getStudyGroupRequestList_success_owner() throws Exception {
        studyMemberRepository.save(new StudyMemberEntity(
                approvalGroup, outsiderUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        mockMvc.perform(get("/api/v1/study/group/{studyId}/request", approvalGroup.getStudyId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("일반 멤버는 신청 목록을 조회할 수 없다")
    void getStudyGroupRequestList_fail_member() throws Exception {
        mockMvc.perform(get("/api/v1/study/group/{studyId}/request", studyGroup.getStudyId())
                        .header("Authorization", memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 신청 승인 ───

    @Test
    @DisplayName("OWNER는 대기 중인 신청을 승인할 수 있다")
    void approveStudyGroupRequest_success() throws Exception {
        StudyMemberEntity pending = studyMemberRepository.save(new StudyMemberEntity(
                approvalGroup, outsiderUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        mockMvc.perform(post("/api/v1/study/group/{studyMemberId}/approval", pending.getStudyMemberId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버는 신청을 승인할 수 없다")
    void approveStudyGroupRequest_fail_member() throws Exception {
        StudyMemberEntity pending = studyMemberRepository.save(new StudyMemberEntity(
                approvalGroup, outsiderUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        mockMvc.perform(post("/api/v1/study/group/{studyMemberId}/approval", pending.getStudyMemberId())
                        .header("Authorization", memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── 스터디 신청 거절 ───

    @Test
    @DisplayName("OWNER는 대기 중인 신청을 거절할 수 있다")
    void rejectStudyGroupRequest_success() throws Exception {
        StudyMemberEntity pending = studyMemberRepository.save(new StudyMemberEntity(
                approvalGroup, outsiderUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        mockMvc.perform(post("/api/v1/study/group/{studyMemberId}/reject", pending.getStudyMemberId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("일반 멤버는 신청을 거절할 수 없다")
    void rejectStudyGroupRequest_fail_member() throws Exception {
        StudyMemberEntity pending = studyMemberRepository.save(new StudyMemberEntity(
                approvalGroup, outsiderUser, StudyMemberRole.ROLE_MEMBER, StudyMemberStatus.STATUS_PENDING));

        mockMvc.perform(post("/api/v1/study/group/{studyMemberId}/reject", pending.getStudyMemberId())
                        .header("Authorization", memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이미 활성 상태인 멤버를 승인하려 하면 실패한다")
    void approveStudyGroupRequest_fail_notPending() throws Exception {
        StudyMemberEntity activeMember = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                studyGroup.getStudyId(), memberUser.getUserId(), StudyMemberStatus.STATUS_ACTIVE
        ).orElseThrow();

        mockMvc.perform(post("/api/v1/study/group/{studyMemberId}/approval", activeMember.getStudyMemberId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
