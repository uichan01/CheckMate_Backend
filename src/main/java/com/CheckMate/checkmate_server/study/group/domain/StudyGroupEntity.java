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
}
