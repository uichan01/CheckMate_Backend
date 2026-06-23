package com.CheckMate.checkmate_server.calendar.personal.repository;

import com.CheckMate.checkmate_server.calendar.personal.domain.UserScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalCalendarRepository extends JpaRepository<UserScheduleEntity, Long> {
    List<UserScheduleEntity> findAllByUserEntity_UserId(Long userId);
}
