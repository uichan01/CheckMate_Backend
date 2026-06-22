package com.CheckMate.checkmate_server.study.category.service;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import com.CheckMate.checkmate_server.study.category.repository.StudyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyCategoryService {
    private final StudyCategoryRepository categoryRepository;
    public Optional<StudyCategoryEntity> getStudyCategoryEntity(long categoryId) {
        return categoryRepository.findById(categoryId);
    }

}
