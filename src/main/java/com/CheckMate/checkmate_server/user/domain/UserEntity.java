package com.CheckMate.checkmate_server.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "intro", length = 255, nullable = false)
    private String intro;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserEntity(String email, String password, String intro, String nickname, UserRole role) {
        this.email = email;
        this.password = password;
        this.intro = intro;
        this.nickname = nickname;
        this.role = role;
    }

    public void updateProfile(String password, String nickname, String intro) {
        if (password != null) {
            this.password = password;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (intro != null) {
            this.intro = intro;
        }
    }
}
