package com.CheckMate.checkmate_server.study.group;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private StudyCategoryEntity category;
    private String userToken;

    @BeforeEach
    void setUp() {
        UserEntity user = userRepository.save(
                new UserEntity("user@test.com", passwordEncoder.encode("pw"), "", "User", UserRole.ROLE_USER));
        category = studyCategoryRepository.save(new StudyCategoryEntity(null, "개발"));
        userToken = "Bearer " + jwtUtil.createJwt("user@test.com", "ROLE_USER", 3_600_000L);
    }

    @Test
    @DisplayName("유효한 요청으로 스터디 그룹을 생성하면 생성된 studyId를 반환한다")
    void createStudyGroup_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "categoryId", category.getCategoryId(),
                "title", "알고리즘 스터디",
                "description", "매주 알고리즘 문제를 풀어요",
                "scope", "SCOPE_PUBLIC",
                "joinPolicy", "JOIN_POLICY_INSTANT"
        ));

        mockMvc.perform(post("/api/v1/study/group")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }
}
