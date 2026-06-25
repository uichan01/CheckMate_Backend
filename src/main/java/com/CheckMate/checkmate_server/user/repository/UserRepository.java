package com.CheckMate.checkmate_server.user.repository;

import com.CheckMate.checkmate_server.domain.DeleteStatus;
import com.CheckMate.checkmate_server.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByNickname(String nickname);

    long deleteByEmail(String email);

    List<UserEntity> findByStatusAndDeleteAtLessThanEqual(
            DeleteStatus status,
            LocalDateTime now
    );
}