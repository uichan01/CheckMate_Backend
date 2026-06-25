package com.CheckMate.checkmate_server.user.auth.service;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.domain.UserRole;
import com.CheckMate.checkmate_server.user.auth.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.auth.dto.res.SignUpResponseDto;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {

    private static final Long TOKEN_EXPIRE_MS = 60 * 60 * 1000L;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        String intro = request.getIntro() != null ? request.getIntro() : "";

        UserEntity user = new UserEntity(
                request.getEmail(),
                encodedPassword,
                intro,
                request.getNickname(),
                UserRole.ROLE_USER
        );

        UserEntity savedUser = userRepository.save(user);

        String token = jwtUtil.createJwt(
                savedUser.getUserId(),
                savedUser.getEmail(),
                UserRole.ROLE_USER.name(),
                TOKEN_EXPIRE_MS
        );
        log.info("{} id로 가입 성공, 이메일: {}", savedUser.getUserId(), savedUser.getEmail());


        return SignUpResponseDto.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .role(savedUser.getRole().name())
                .token(token)
                .build();


    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 물리삭제
//        userRepository.delete(user);

        // soft delete
        user.delete(LocalDateTime.now());
    }

    @Override
    public boolean checkEmailDuplication(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
