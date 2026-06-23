package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroupEntity, Long> {
    List<StudyGroupEntity> findByTitleContaining(String keyword);

    List<StudyGroupEntity> findByCategoryEntity_CategoryId(Long categoryId);

    List<StudyGroupEntity> findByTitleContainingAndCategoryEntity_CategoryId(
            String keyword,
            Long categoryId
    );
}
