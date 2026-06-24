package com.CheckMate.checkmate_server.study.category.service;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.dto.res.StudyCategoryResponseDto;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyCategoryService {
    private final StudyCategoryRepository categoryRepository;

    // 카테고리 추가
    @Transactional
    public Long addStudyCategoryEntity(String categoryName) {
        StudyCategoryEntity entity = categoryRepository.save(StudyCategoryEntity.builder()
                        .categoryName(categoryName)
                .build());
        return entity.getCategoryId();
    }

    // 카테고리 전체 조회
    @Transactional
    public List<StudyCategoryResponseDto> getCategories() {
        return categoryRepository.findAll().stream().map(StudyCategoryResponseDto::from).toList();
    }

}
