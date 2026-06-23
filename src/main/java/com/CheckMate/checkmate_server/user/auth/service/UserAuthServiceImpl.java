package com.CheckMate.checkmate_server.user.auth.service;

import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.domain.UserRole;
import com.CheckMate.checkmate_server.user.auth.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.auth.dto.res.SignUpResponseDto;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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
    public void deleteUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        userRepository.delete(user);
    }

    @Override
    public boolean checkEmailDuplication(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
