package com.CheckMate.checkmate_server.study.group.service;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.service.StudyCategoryService;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupSearchRequest;
import com.CheckMate.checkmate_server.study.group.dto.res.StudyGroupResponseDto;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final StudyCategoryService studyCategoryService;

    // 스터디 그룹 생성
    public Long createStudyGroup(@NonNull StudyGroupRequestDto request) {
        // 카테고리id를 받아서 엔티티 획득
        Optional<StudyCategoryEntity> studyCategoryEntity = studyCategoryService.getStudyCategoryEntity(request.getCategoryId());
        // 해당 엔티티가 없으면 예외 throw
        StudyCategoryEntity categoryEntity = studyCategoryEntity
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        StudyGroupEntity entity = StudyGroupEntity.create(
                categoryEntity,
                request.getTitle(),
                request.getDescription(),
                request.getScope(),
                request.getJoinPolicy()
        );
        // 생성
        StudyGroupEntity savedEntity = studyGroupRepository.save(entity);
        // 스터디id 반환
        return savedEntity.getStudyId();
    }

    // 조건에 맞게 목록 조회
    public List<StudyGroupResponseDto> searchStudyGroups(StudyGroupSearchRequest request) {
        String keyword = request.getKeyword();
        Long categoryId = request.getCategoryId();

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;

        List<StudyGroupEntity> studyGroups;

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

        return studyGroups.stream()
                .map(StudyGroupResponseDto::from)
                .toList();
    }

}
