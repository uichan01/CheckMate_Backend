package com.CheckMate.checkmate_server.user.account.repository;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMembershipRepository extends JpaRepository<StudyMemberEntity, Long> {

    List<StudyMemberEntity> findByUserEntity_UserIdAndStatus(Long userId, StudyMemberStatus status);
}
