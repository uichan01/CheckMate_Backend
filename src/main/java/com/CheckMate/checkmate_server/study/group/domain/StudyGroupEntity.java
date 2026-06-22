package com.CheckMate.checkmate_server.study.group.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="study_groups")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroupEntity {
    public StudyGroupEntity(Long categoryId, String title, String description, GroupScope scope, GroupJoinPolicy joinPolicy) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.scope = scope;
        this.joinPolicy = joinPolicy;
    }
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="group_id")
    private Long groupId;

    @Column(name="category_id", nullable = false)
    private Long categoryId;

    @Column(name="title", length = 50, nullable = false)
    private String title;

    @Column(name ="description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name="scope", length = 20, nullable = false)
    private GroupScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_policy", length = 20, nullable = false)
    private GroupJoinPolicy joinPolicy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static StudyGroupEntity create(
            Long categoryId,
            String title,
            String description,
            GroupScope scope,
            GroupJoinPolicy joinPolicy
    ) {
        return new StudyGroupEntity(
                categoryId,
                title,
                description,
                scope,
                joinPolicy
        );
    }
}
