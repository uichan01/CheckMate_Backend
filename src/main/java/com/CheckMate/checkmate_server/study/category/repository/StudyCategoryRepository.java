package com.CheckMate.checkmate_server.study.category.repository;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyCategoryRepository extends JpaRepository<StudyCategoryEntity, Long> {
}
