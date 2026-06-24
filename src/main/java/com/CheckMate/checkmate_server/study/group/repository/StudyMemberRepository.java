package com.CheckMate.checkmate_server.study.group.repository;

import com.CheckMate.checkmate_server.dashboard.dto.res.DashboardUserResponseDto;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMemberEntity, Long> {
    Optional<StudyMemberEntity> findByStudyGroupEntity_StudyIdAndUserEntity_UserId(
            Long studyId,
            Long userId
    );
    List<StudyMemberEntity> findByUserEntity_UserIdAndStatus(Long userId, StudyMemberStatus status);

    List<StudyMemberEntity> findByStudyGroupEntity_StudyIdAndStatus(
            Long studyId,
            StudyMemberStatus status
    );
    Optional<StudyMemberEntity> findByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
            Long studyId,
            Long memberId,
            StudyMemberStatus status
    );
    boolean existsByStudyGroupEntity_StudyIdAndUserEntity_UserIdAndStatus(
            Long studyId,
            Long userId,
            StudyMemberStatus status
    );

    // 각 유저 별 제출 수 반환
    @Query("""
    select new com.CheckMate.checkmate_server.dashboard.dto.query.DashboardUserSubmissionCountDto(
        sm.userEntity.userId,
        sm.userEntity.nickname,
        count(distinct ts.taskEntity.taskId)
    )
    from StudyMemberEntity sm
    left join TaskSubmissionEntity ts
        on ts.userEntity = sm.userEntity
       and ts.taskEntity.studyGroupEntity.studyId = :studyId
    where sm.studyGroupEntity.studyId = :studyId
      and sm.status = :status
    group by sm.userEntity.userId, sm.userEntity.nickname
""")
    List<DashboardUserResponseDto> findUserSubmissionCountsByStudyId(
            @Param("studyId") Long studyId,
            @Param("status") StudyMemberStatus status
    );

}
