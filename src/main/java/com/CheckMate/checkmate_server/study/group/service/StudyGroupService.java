package com.CheckMate.checkmate_server.study.group.service;

import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import com.CheckMate.checkmate_server.study.group.dto.req.StudyGroupRequestDto;
import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;

    public Long createStudyGroup(@NonNull StudyGroupRequestDto request) {
        StudyGroupEntity entity = StudyGroupEntity.create(
                request.getCategoryId(),
                request.getTitle(),
                request.getDescription(),
                request.getScope(),
                request.getJoinPolicy()
        );
        StudyGroupEntity savedEntity = studyGroupRepository.save(entity);

        return savedEntity.getGroupId();
    }
}
