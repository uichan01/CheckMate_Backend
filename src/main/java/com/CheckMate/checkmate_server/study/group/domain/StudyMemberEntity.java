package com.CheckMate.checkmate_server.study.group.domain;

import com.CheckMate.checkmate_server.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="study_members")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMemberEntity {
    public StudyMemberEntity(StudyGroupEntity studyGroupEntity, UserEntity userEntity, StudyMemberRole role, StudyMemberStatus status) {
        this.studyGroupEntity = studyGroupEntity;
        this.userEntity = userEntity;
        this.role = role;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="study_member_id")
    private Long studyMemberId;

    @ManyToOne
    @JoinColumn(name ="study_id")
    private StudyGroupEntity studyGroupEntity;

    @ManyToOne
    @JoinColumn(name ="user_id")
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private StudyMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudyMemberStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void setRoleMember() {
        this.role = StudyMemberRole.ROLE_MEMBER;
    }

    public void setRoleManager() {
        this.role = StudyMemberRole.ROLE_MANAGER;
    }

    public void active() {
        this.status = StudyMemberStatus.STATUS_ACTIVE;
    }
    public void left() {
        this.status = StudyMemberStatus.STATUS_LEFT;

    }
    public void ban() {
        this.status = StudyMemberStatus.STATUS_BANNED;
    }

}
