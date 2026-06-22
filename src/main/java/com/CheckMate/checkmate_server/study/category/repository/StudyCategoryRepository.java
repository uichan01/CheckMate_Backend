package com.CheckMate.checkmate_server.study.category.repository;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyCategoryRepository extends JpaRepository<StudyCategoryEntity, Long> {
}
