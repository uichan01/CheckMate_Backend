package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMemberEntity, Long> {
    Optional<StudyMemberEntity> findByStudy_StudyIdAndUser_Email(Long studyId, String email);
    Optional<StudyMemberEntity> findByStudyGroupEntity_GroupIdAndUserEntity_Email(
            Long groupId,
            String email
    );
    List<StudyMemberEntity> findByStudyGroupEntity_StudyIdAndStatus(
            Long studyId,
            StudyMemberStatus status
    );
}