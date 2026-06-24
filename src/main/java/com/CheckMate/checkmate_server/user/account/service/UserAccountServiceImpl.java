package com.CheckMate.checkmate_server.user.account.service;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.user.account.dto.req.UpdateMyInfoRequestDto;
import com.CheckMate.checkmate_server.user.account.dto.res.MyInfoResponseDto;
import com.CheckMate.checkmate_server.user.account.dto.res.MyStudyDto;
import com.CheckMate.checkmate_server.user.account.dto.res.UserInfoResponseDto;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional(readOnly = true)
    public MyInfoResponseDto getMyInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<MyStudyDto> studies = studyMemberRepository
                .findByUserEntity_UserIdAndStatus(userId, StudyMemberStatus.STATUS_ACTIVE)
                .stream()
                .map(MyStudyDto::from)
                .toList();

        return MyInfoResponseDto.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .role(user.getRole().name())
                .studies(studies)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(String nickname) {
        UserEntity user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<MyStudyDto> studies = studyMemberRepository
                .findByUserEntity_UserIdAndStatus(user.getUserId(), StudyMemberStatus.STATUS_ACTIVE)
                .stream()
                .map(MyStudyDto::from)
                .toList();

        return UserInfoResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .role(user.getRole().name())
                .studies(studies)
                .build();
    }

    @Override
    @Transactional
    public void updateMyInfo(Long userId, UpdateMyInfoRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String encodedPassword = request.getPassword() != null
                ? bCryptPasswordEncoder.encode(request.getPassword())
                : null;

        user.updateProfile(encodedPassword, request.getNickname(), request.getIntro());
    }
}
