package com.CheckMate.checkmate_server.study.group.domain;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
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

//    @Column(name="category_id", nullable = false)
//    private Long categoryId;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static StudyGroupEntity create(
            StudyCategoryEntity categoryEntity,
            String title,
            String description,
            GroupScope scope,
            GroupJoinPolicy joinPolicy
    ) {
        return new StudyGroupEntity(
                categoryEntity,
                title,
                description,
                scope,
                joinPolicy
        );
    }
}
