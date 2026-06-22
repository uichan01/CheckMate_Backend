package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMemberEntity, Long> {
    Optional<StudyMemberEntity> findByStudy_StudyIdAndUser_Email(Long studyId, String email);
    Optional<StudyMemberEntity> findByStudyGroupEntity_GroupIdAndUserEntity_Email(
            Long groupId,
            String email
    );
}
