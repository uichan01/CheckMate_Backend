package com.CheckMate.checkmate_server.study.group.dto.res;

import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupDetailResponseDto {
    private String title;
    private String description;
    private String categoryName;
    private LocalDateTime createdAt;

    public static StudyGroupDetailResponseDto from(StudyGroupEntity groupEntity) {
        return new StudyGroupDetailResponseDto(
                groupEntity.getTitle(),
                groupEntity.getDescription(),
                groupEntity.getCategoryEntity().getCategoryName(),
                groupEntity.getCreatedAt()
        );
    }
}
