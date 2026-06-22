package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMemberEntity, Long> {

}
