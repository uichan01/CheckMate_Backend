package com.CheckMate.checkmate_server.user.service;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.domain.UserRole;
import com.CheckMate.checkmate_server.user.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.dto.res.SignUpResponseDto;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

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
                savedUser.getEmail(),
                UserRole.ROLE_USER.name(),
                TOKEN_EXPIRE_MS
        );

        return SignUpResponseDto.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .role(savedUser.getRole().name())
                .token(token)
                .build();
    }
}
