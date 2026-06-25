package com.CheckMate.checkmate_server.study.group.domain;

import com.CheckMate.checkmate_server.domain.DeleteStatus;
import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="study_groups")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupEntity {
    public StudyGroupEntity(StudyCategoryEntity categoryEntity, String title, String description, GroupScope scope, GroupJoinPolicy joinPolicy) {
        this.categoryEntity = categoryEntity;
        this.title = title;
        this.description = description;
        this.scope = scope;
        this.joinPolicy = joinPolicy;
    }
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="study_id")
    private Long studyId;

    @ManyToOne
    @JoinColumn(name ="category_id")
    private StudyCategoryEntity categoryEntity;

    @Column(name="title", length = 50, nullable = false)
    private String title;

    @Column(name ="description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name="scope", nullable = false)
    private GroupScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_policy", nullable = false)
    private GroupJoinPolicy joinPolicy;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeleteStatus status;

    @Column(name="delete_at")
    private LocalDateTime deleteAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // put
    public void update(
            StudyCategoryEntity categoryEntity,
            String title,
            String description,
            GroupScope scope,
            GroupJoinPolicy joinPolicy
    ) {
        this.categoryEntity = categoryEntity;
        this.title = title;
        this.description = description;
        this.scope = scope;
        this.joinPolicy = joinPolicy;
    }

    // patch
    public void updatePartial(
            StudyCategoryEntity categoryEntity,
            String title,
            String description,
            GroupScope scope,
            GroupJoinPolicy joinPolicy
    ) {
        if(categoryEntity != null)
            this.categoryEntity = categoryEntity;
        if(title != null && !title.isBlank())
            this.title = title;
        if(description != null && !description.isBlank())
            this.description = description;
        if (scope != null)
            this.scope = scope;
        if (joinPolicy != null)
            this.joinPolicy = joinPolicy;
    }

    // delete
    public void delete(LocalDateTime now) {
        this.status = DeleteStatus.STATUS_DELETE_PENDING;
        // 3일 뒤 삭제
        this.deleteAt = now.plusDays(3);
    }
}
