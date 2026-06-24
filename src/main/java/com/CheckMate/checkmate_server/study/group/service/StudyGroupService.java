package com.CheckMate.checkmate_server.study.group.service;

import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import com.CheckMate.checkmate_server.study.category.service.StudyCategoryService;
import com.CheckMate.checkmate_server.study.group.domain.*;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupPatchRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupSearchRequest;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupDetailResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupRequestResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupResponseDto;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyMemberDto;
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
    private final StudyCategoryRepository categoryRepository;

    // 스터디 그룹 생성
    @Transactional
    public Long createStudyGroup(@NonNull StudyGroupRequestDto request, Long userId) {
        // 카테고리id를 받아서 엔티티 획득
        StudyCategoryEntity categoryEntity = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 카테고가 없습니다."));

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

        log.info("{} 스터디 그룹 생성", savedEntity.getStudyId());

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
        log.info("스터디 그룹 목록 조건 조회 요청");
        // Entity를 ResponseDto로 변환하며 List로 반환
        return studyGroups.stream()
                .map(StudyGroupResponseDto::from)
                .toList();
    }
    
    // 스터디 그룹의 상세 조회
    @Transactional
    public StudyGroupDetailResponseDto getStudyGroupDetails(Long studyId, Long userId) {
        // 스터디id로 스터디그룹엔티티 획득
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스터디 그룹은 존재하지 않습니다."));

        // 비공개거나 공유 전용 페이지는 권한이 있어야 볼 수 있음
        if(studyGroupEntity.getScope() == GroupScope.SCOPE_PRIVATE
                || studyGroupEntity.getScope() == GroupScope.SCOPE_SHARED) {
            if(!studyMemberRepository.existsByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                    studyId,
                    userId,
                    StudyMemberStatus.STATUS_ACTIVE
            )) {
                throw new IllegalArgumentException("스터디에 접근할 권한이 없습니다.");
            }
        }

        // 해당 스터디 그룹에 속한 멤버 목록 획득
        List<StudyMemberDto> studyMembers = studyMemberRepository.findByStudyGroupEntity_StudyIdAndStatus(
                studyId, StudyMemberStatus.STATUS_ACTIVE)
                .stream().map((member) -> StudyMemberDto.builder()
                        .userId(member.getUserEntity().getUserId())
                        .nickName(member.getUserEntity().getNickname())
                        .role(member.getRole())
                        .build()
        ).toList();

        log.info("{} 스터디 상세 조회 요청", studyId);
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
            categoryEntity = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("유효한 카테고가 없습니다."));
        }
        // studyId에 맞는 스터디 그룹을 찾고, 없으면 예외 Throw
        StudyGroupEntity studyGroup = studyGroupRepository.findById(studyId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 스터디 그룹입니다."));

        // 수정
        studyGroup.updatePartial(categoryEntity, request.getTitle(), request.getDescription(), request.getScope(), request.getJoinPolicy());

        log.info("{}스터디 수정 요청 완료", studyId);
        // 스터디id 반환
        return studyGroup.getStudyId();
    }
    
    // 스터디 멤버 초대
    @Transactional
    public Long addStudyMember(Long studyId, String memberEmail, Long userId) {
        memberEmail = memberEmail.trim();

        // 자신의 권한 확인(소유자, 관리자)
        validateStudyOwnerOrManager(studyId, userId);
        log.info("{} 유저 초대 시작", memberEmail);
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

    // 스터디 멤버 제거
    @Transactional
    public Long removeStudyMember(Long studyId, Long memberId, Long userId) {

        // 자신의 권한 확인(소유자, 관리자)
        StudyMemberEntity studyMemberEntity = validateStudyOwnerOrManager(studyId, userId);

        // 해당 member가 존재하는지 검증
        if(!userRepository.existsById(memberId))
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");

        // studyId에 맞는 스터디 그룹을 찾고, 없으면 예외 Throw
        StudyGroupEntity studyGroup = studyGroupRepository.findById(studyId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 스터디 그룹입니다."));
        
        // 해당 멤버가 존재하는지 확인
        StudyMemberEntity targetStudyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                studyId,
                memberId,
                StudyMemberStatus.STATUS_ACTIVE
        ).orElseThrow(()->new IllegalArgumentException("스터디 그룹에 해당 멤버가 존재하지 않습니다."));

        // 타겟과 자기 자신이 같은 경우
        if(userId.equals(memberId))
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
        targetStudyMemberEntity.left();

        // 로깅
        log.warn("{}가 {}를 {}그룹에서 삭제", userId, memberId, studyGroup.getStudyId());
        return studyMemberEntity.getStudyMemberId();
    }

    // 자신이 속한 스터디 조회
    @Transactional
    public List<StudyGroupResponseDto> getMyStudyGroups(Long userId) {
        log.info("{} 유저가 속한 스터디 그룹 조회 요청", userId);
        // Entity를 ResponseDto로 변환하며 List로 반환
        return studyMemberRepository.findByUserEntity_UserIdAndStatus(
                userId,
                StudyMemberStatus.STATUS_ACTIVE
        ).stream().map(StudyMemberEntity::getStudyGroupEntity).map(StudyGroupResponseDto::from).toList();
    }

    // 스터디 역할 변경
    @Transactional
    public Long setStudyMemberRole(Long studyId, Long memberId, StudyMemberRole role, Long userId) {
//        memberEmail = memberEmail.trim();
        // 자신의 권한 검증
        validateStudyOwner(studyId, userId);

        if (role == null) {
            throw new IllegalArgumentException("변경할 역할은 필수입니다.");
        }
        // member 이메일 검증
        UserEntity targetUserEntity = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        StudyMemberEntity targetEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                studyId,
                targetUserEntity.getUserId(),
                StudyMemberStatus.STATUS_ACTIVE
        ).orElseThrow(()->new IllegalArgumentException("해당 스터디 그룹에 유효한 멤버가 없습니다."));

        if(targetUserEntity.getUserId().equals(userId))
            throw new IllegalArgumentException("자기 자신의 역할을 변경할 수 없습니다.");

        if (targetEntity.getRole() == StudyMemberRole.ROLE_OWNER) {
            throw new IllegalArgumentException("스터디 소유자의 역할은 변경할 수 없습니다.");
        }

        switch (role) {
            case ROLE_MANAGER -> targetEntity.setRoleManager();
            case ROLE_MEMBER -> targetEntity.setRoleMember();
            default -> throw new IllegalArgumentException("해당 역할로는 변경이 불가능합니다.");
        }

        return targetEntity.getStudyMemberId();
    }

    // 스터디 그룹 신청
    @Transactional
    public StudyMemberStatus requestStudyGroup(Long studyId, Long userId) {
        // 스터디 그룹 획득
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));

        // 스터디 그룹에 가입 가능한지
        if(studyGroupEntity.getScope() == GroupScope.SCOPE_PRIVATE || studyGroupEntity.getScope() == GroupScope.SCOPE_SHARED)
            throw new IllegalArgumentException("비공개 스터디에는 가입 신청할 수 없습니다.");

        // 스터디 그룹의 가입 정책에 따른 초기 상태 지정
        final StudyMemberStatus initialStatus = (studyGroupEntity.getJoinPolicy() == GroupJoinPolicy.JOIN_POLICY_INSTANT) ?
                StudyMemberStatus.STATUS_ACTIVE : StudyMemberStatus.STATUS_PENDING;

        // 자신이 이미 가입중, 신청중, 차단된 스터디는 신청 불가
        Optional<StudyMemberEntity> optionalStudyMember = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserId(
                studyId,
                userId
        );

        if(optionalStudyMember.isPresent()) {
            StudyMemberEntity studyMember = optionalStudyMember.get();
            switch (studyMember.getStatus()) {
                case STATUS_ACTIVE -> throw new IllegalArgumentException("이미 가입된 스터디입니다.");
                case STATUS_PENDING -> throw new IllegalArgumentException("이미 신청한 스터디입니다.");
                case STATUS_BANNED -> throw new IllegalArgumentException("차단당한 스터디입니다.");
                case STATUS_LEFT -> studyMember.pending();
                default -> throw new IllegalArgumentException("존재하지 않는 상태입니다.");
            }
            // 즉시 승인되는 스터디는 즉시 활성화되도록
            if(studyGroupEntity.getJoinPolicy() == GroupJoinPolicy.JOIN_POLICY_INSTANT)
                studyMember.active();

            // 로깅
            log.info("{} 사용자가 {} 스터디 그룹에 가입 신청", userId, studyId);
            return studyMember.getStatus();
        }

        // 새로운 객체 생성
        StudyMemberEntity studyMember = StudyMemberEntity.builder()
                .studyGroupEntity(studyGroupEntity)
                .userEntity(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다.")))
                .role(StudyMemberRole.ROLE_MEMBER)
                .status(initialStatus)
                .build();
        // 추가
        studyMemberRepository.save(studyMember);

        // 로깅
        log.info("{} 사용자가 {} 스터디 그룹에 가입 신청", userId, studyId);
        // 현 상태 반환
        return studyMember.getStatus();
    }

    // 스터디 신청 목록 조회
    @Transactional
    public List<StudyGroupRequestResponseDto> getStudyGroupRequestList(Long studyId, Long userId) {
        studyGroupRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 스터디 그룹입니다.")));
        
        // 권한 확인
        validateStudyOwnerOrManager(studyId, userId);

        List<StudyGroupRequestResponseDto> studyMemberEntityList = studyMemberRepository.findByStudyGroupEntity_StudyIdAndStatus(
                studyId,
                StudyMemberStatus.STATUS_PENDING
        ).stream().map(studyMemberEntity -> {
            return StudyGroupRequestResponseDto.builder()
                    .studyMemberId(studyMemberEntity.getStudyMemberId())
                    .userId(studyMemberEntity.getUserEntity().getUserId())
                    .email(studyMemberEntity.getUserEntity().getEmail())
                    .nickname(studyMemberEntity.getUserEntity().getNickname())
                    .requestDate(studyMemberEntity.getCreatedAt())
                    .build();
        }).toList();

        return studyMemberEntityList;
    }

    // 가입 요청 승인
    @Transactional
    public void approveStudyGroupRequest(Long studyMemberId, Long userId) {
        // 해당 번호를 가져오고, 스터디 그룹을 가져옴
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findById(studyMemberId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 번호입니다."));
        StudyGroupEntity groupEntity = studyMemberEntity.getStudyGroupEntity();
        //권한 확인
//        StudyMemberEntity myStudyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
//                groupEntity.getStudyId(),
//                userId,
//                StudyMemberStatus.STATUS_ACTIVE
//        ).orElseThrow(() -> new IllegalArgumentException("자신이 스터디에 속해있지 않습니다."));
//        if(myStudyMemberEntity.getRole() == StudyMemberRole.ROLE_MEMBER)
//            throw new IllegalArgumentException("권한이 없습니다.");
        validateStudyOwnerOrManager(groupEntity.getStudyId(),userId);

        // 현재 상태가 PENDING인지 확인
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("신청 상태가 아닙니다.");
        // 승인
        studyMemberEntity.active();
    }

    // 가입 요청 거절
    @Transactional
    public void rejectStudyGroupRequest(Long studyMemberId, Long userId) {
        // 해당 번호를 가져오고, 스터디 그룹을 가져옴
        StudyMemberEntity studyMemberEntity = studyMemberRepository.findById(studyMemberId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 번호입니다."));
        StudyGroupEntity groupEntity = studyMemberEntity.getStudyGroupEntity();
        //권한 확인
        StudyMemberEntity myStudyMemberEntity = studyMemberRepository.findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
                groupEntity.getStudyId(),
                userId,
                StudyMemberStatus.STATUS_ACTIVE
        ).orElseThrow(() -> new IllegalArgumentException("자신이 스터디에 속해있지 않습니다."));

        if(myStudyMemberEntity.getRole() == StudyMemberRole.ROLE_MEMBER)
            throw new IllegalArgumentException("권한이 없습니다.");

        // 현재 상태가 PENDING인지 확인
        if(studyMemberEntity.getStatus() != StudyMemberStatus.STATUS_PENDING)
            throw new IllegalArgumentException("신청 상태가 아닙니다.");

        // 거부 (물리 삭제)
        studyMemberRepository.delete(studyMemberEntity);
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
                .orElseThrow(() -> new IllegalArgumentException(userId + "는 " + studyId + "스터디 멤버가 아닙니다."));

        if (studyMember.getRole() != StudyMemberRole.ROLE_OWNER && studyMember.getRole() != StudyMemberRole.ROLE_MANAGER) {
            throw new IllegalArgumentException("권한이 부족합니다.");
        }
        return studyMember;
    }



}
