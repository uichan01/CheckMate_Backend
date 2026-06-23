package com.CheckMate.checkmate_server.study.group.service;

import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.service.StudyCategoryService;
import com.CheckMate.checkmate_server.study.dto.SimpleUserDto;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberRole;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupPatchRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupSearchRequest;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupDetailResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupResponseDto;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.group.repository.StudyMemberRepository;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyCategoryService studyCategoryService;
    private final UserRepository userRepository;

    // 스터디 그룹 생성
    @Transactional
    public Long createStudyGroup(@NonNull StudyGroupRequestDto request, Long userId) {
        // 카테고리id를 받아서 엔티티 획득
        Optional<StudyCategoryEntity> studyCategoryEntity = studyCategoryService.getStudyCategoryEntity(request.getCategoryId());

        // 해당 엔티티가 없으면 예외 throw
        StudyCategoryEntity categoryEntity = studyCategoryEntity
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 스터디 그룹 엔티티 생성
        StudyGroupEntity entity = StudyGroupEntity.builder()
                .categoryEntity(categoryEntity)
                .title(request.getTitle())
                .description(request.getDescription())
                .scope(request.getScope())
                .joinPolicy(request.getJoinPolicy())
                .build();
        // 생성
        StudyGroupEntity savedEntity = studyGroupRepository.save(entity);
        // userId를 기반으로 userEntity를 찾고, 없으면 예외 throw
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        // 자신을 스터디 장으로 임명
        StudyMemberEntity memberEntity = StudyMemberEntity.builder()
                .studyGroupEntity(savedEntity)
                .userEntity(userEntity)
                .role(StudyMemberRole.ROLE_OWNER)
                .status(StudyMemberStatus.STATUS_ACTIVE)
                .build();

        // 스터디 멤버에 저장
        studyMemberRepository.save(memberEntity);

        // 스터디id 반환
        return savedEntity.getStudyId();
    }

    // 스터디 그룹을 조건에 맞게 목록 조회
    @Transactional
    public List<StudyGroupResponseDto> searchStudyGroups(StudyGroupSearchRequest request) {
        // 키워드와 카테고리 id 획득
        String keyword = request.getKeyword();
        Long categoryId = request.getCategoryId();

        // 키워드와 카테고리id 존재 여부
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;

        List<StudyGroupEntity> studyGroups;
        // 값의 존재 여부에 따라 적절한 메소드로 find 호출
        if (hasKeyword && hasCategory) {
            studyGroups = studyGroupRepository
                    .findByTitleContainingAndCategoryEntity_CategoryId(keyword, categoryId);
        } else if (hasKeyword) {
            studyGroups = studyGroupRepository
                    .findByTitleContaining(keyword);
        } else if (hasCategory) {
            studyGroups = studyGroupRepository
                    .findByCategoryEntity_CategoryId(categoryId);
        } else {
            studyGroups = studyGroupRepository.findAll();
        }
        // Entity를 ResponseDto로 변환하며 List로 반환
        return studyGroups.stream()
                .map(StudyGroupResponseDto::from)
                .toList();
    }
    
    // 스터디 그룹의 상세 조회
    @Transactional
    public StudyGroupDetailResponseDto getStudyGroupDetails(Long studyId) {
        // 스터디id로 스터디그룹엔티티 획득
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 그룹은 존재하지 않습니다."));

        // 해당 스터디 그룹에 속한 멤버 목록 획득
        List<SimpleUserDto> studyMembers = studyMemberRepository.findByStudyGroupEntity_StudyIdAndStatus(
                studyId, StudyMemberStatus.STATUS_ACTIVE)
                .stream().map((member) -> SimpleUserDto.builder()
                        .userId(member.getUserEntity().getUserId())
                        .nickName(member.getUserEntity().getNickname())
                        .build()
        ).toList();
        return StudyGroupDetailResponseDto.from(studyGroupEntity, studyMembers);
    }

    // 스터디 수정
    @Transactional
    public Long updateStudyGroup(Long studyId, @NonNull StudyGroupPatchRequestDto request, Long userId) {
        // 내 소유의 스터디 그룹인지 확인
        validateStudyOwner(studyId, userId);

        StudyCategoryEntity categoryEntity = null;
        if(request.getCategoryId() != null) {
            // 카테고리id를 받아서 엔티티 획득
            Optional<StudyCategoryEntity> studyCategoryEntity = studyCategoryService.getStudyCategoryEntity(request.getCategoryId());

            // 해당 엔티티가 없으면 예외 throw
            categoryEntity = studyCategoryEntity
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        }
        // studyId에 맞는 스터디 그룹을 찾고, 없으면 예외 Throw
        StudyGroupEntity studyGroup = studyGroupRepository.findById(studyId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 스터디 그룹입니다."));

        // 수정
        studyGroup.updatePartial(categoryEntity, request.getTitle(), request.getDescription(), request.getScope(), request.getJoinPolicy());

        // 스터디id 반환
        return studyGroup.getStudyId();
    }

    @Transactional
    public Long addStudyMember(Long studyId, String memberEmail, Long userId) {
        // 자신의 권한 확인(소유자, 관리자)
        validateStudyOwnerOrManager(studyId, userId);

        // 해당 member이메일이 존재하는지 검증
        UserEntity userEntity = userRepository.findByEmail(memberEmail).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        // studyId에 맞는 스터디 그룹을 찾고, 없으면 예외 Throw
        StudyGroupEntity studyGroup = studyGroupRepository.findById(studyId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 스터디 그룹입니다."));

        // 엔티티 생성, 곧바로 Active
        StudyMemberEntity entity = StudyMemberEntity.builder()
                .studyGroupEntity(studyGroup)
                .userEntity(userEntity)
                .role(StudyMemberRole.ROLE_MEMBER)
                .status(StudyMemberStatus.STATUS_ACTIVE)
                .build();
        studyMemberRepository.save(entity);

        // 로깅
        log.info("{}가 {}를 {}그룹에 초대", userId, userEntity.getUserId(), studyGroup.getStudyId());
        return entity.getStudyMemberId();
    }

    @Transactional
    public Long removeStudyMember(Long studyId, String memberEmail, Long userId) {
        // 자신의 권한 확인(소유자, 관리자)
        StudyMemberEntity studyMemberEntity = validateStudyOwnerOrManager(studyId, userId);

        // 해당 member이메일이 존재하는지 검증
        UserEntity targetUserEntity = userRepository.findByEmail(memberEmail).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        // studyId에 맞는 스터디 그룹을 찾고, 없으면 예외 Throw
        StudyGroupEntity studyGroup = studyGroupRepository.findById(studyId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 스터디 그룹입니다."));
        
        // 해당 멤버가 존재하는지 확인
        StudyMemberEntity targetStudyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                studyId,
                targetUserEntity.getUserId(),
                StudyMemberStatus.STATUS_ACTIVE
        ).orElseThrow(()->new IllegalArgumentException("스터디 그룹에 해당 멤버가 존재하지 않습니다."));

        // 타겟과 자기 자신이 같은 경우
        if(userId.equals(targetUserEntity.getUserId()))
            throw new IllegalArgumentException("자기자신은 삭제할 수 없습니다.");

//        studyMemberRepository.delete(studyMemberEntity);
        // 권한 검증
        StudyMemberRole targetMemberRole = targetStudyMemberEntity.getRole();
        StudyMemberRole myMemberRole = studyMemberEntity.getRole();

        if(targetMemberRole == StudyMemberRole.ROLE_OWNER) {
            throw new IllegalArgumentException("소유자는 제거할 수 없습니다.");
        }
        else {
            if(targetMemberRole == myMemberRole) {
                throw new IllegalArgumentException("권한이 부족합니다.");
            }
        }
        // soft delete
        studyMemberEntity.left();

        // 로깅
        log.warn("{}가 {}를 {}그룹에서 삭제", userId, targetUserEntity.getUserId(), studyGroup.getStudyId());
        return targetUserEntity.getUserId();
    }
    ////////////////////////////////////////////////////////////////////////////////////

    // 주어진 studyId에 대한 자신의 Owner 권한 확인
    private StudyMemberEntity validateStudyOwner(Long studyId, Long userId) {
        StudyMemberEntity studyMember = studyMemberRepository
                .findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                        studyId,
                        userId,
                        StudyMemberStatus.STATUS_ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("스터디 멤버가 아닙니다."));

        if (studyMember.getRole() != StudyMemberRole.ROLE_OWNER) {
            throw new IllegalArgumentException("권한이 부족합니다.");
        }
        return studyMember;
    }

    // 주어진 studyId에 대한 자신의 Owner 혹은 Manager 권한 확인
    private StudyMemberEntity validateStudyOwnerOrManager(Long studyId, Long userId) {
        StudyMemberEntity studyMember = studyMemberRepository
                .findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                        studyId,
                        userId,
                        StudyMemberStatus.STATUS_ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("스터디 멤버가 아닙니다."));

        if (studyMember.getRole() != StudyMemberRole.ROLE_OWNER && studyMember.getRole() != StudyMemberRole.ROLE_MANAGER) {
            throw new IllegalArgumentException("권한이 부족합니다.");
        }
        return studyMember;
    }

}
