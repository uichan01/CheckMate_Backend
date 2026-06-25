package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.domain.DeleteStatus;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroupEntity, Long> {
//    List<StudyGroupEntity> findByTitleContaining(String keyword);
//
//    List<StudyGroupEntity> findByCategoryEntity_CategoryId(Long categoryId);
//
//    List<StudyGroupEntity> findByTitleContainingAndCategoryEntity_CategoryId(
//            String keyword,
//            Long categoryId
//    );
    
    // 활성화된 그룹만 조회
    List<StudyGroupEntity> findByTitleContainingAndCategoryEntity_CategoryIdAndStatus(String keyword, Long categoryId, DeleteStatus status);
    List<StudyGroupEntity> findByTitleContainingAndStatus(String keyword, DeleteStatus deleteStatus);
    List<StudyGroupEntity> findByCategoryEntity_CategoryIdAndStatus(Long categoryId, DeleteStatus deleteStatus);

    List<StudyGroupEntity> findByStatus(DeleteStatus groupStatus);

    // 삭제 예정 그룹 조회
    List<StudyGroupEntity> findByStatusAndDeleteAtLessThanEqual(
            DeleteStatus status,
            LocalDateTime now
    );
}
