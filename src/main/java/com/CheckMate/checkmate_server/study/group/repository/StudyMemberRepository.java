package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMemberEntity, Long> {
    Optional<StudyMemberEntity> findByStudyGroupEntity_StudyIdAndUserEntity_Email(
            Long studyId,
            String email
    );
    List<StudyMemberEntity> findByUserEntity_UserIdAndStatus(Long userId, StudyMemberStatus status);
    List<StudyMemberEntity> findByStudyGroupEntity_StudyIdAndStatus(
            Long studyId,
            StudyMemberStatus status
    );
}
