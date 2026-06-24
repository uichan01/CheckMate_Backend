package com.CheckMate.checkmate_server.study.group.dto.res;

import com.CheckMate.checkmate_server.study.dto.SimpleUserDto;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import com.CheckMate.checkmate_server.study.group.domain.StudyMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupDetailResponseDto {
    private String title;
    private String description;
    private String categoryName;
    private LocalDateTime createdAt;
    private List<StudyMemberDto> members;

    public static StudyGroupDetailResponseDto from(StudyGroupEntity groupEntity, List<StudyMemberDto> members) {
        return new StudyGroupDetailResponseDto(
                groupEntity.getTitle(),
                groupEntity.getDescription(),
                groupEntity.getCategoryEntity().getCategoryName(),
                groupEntity.getCreatedAt(),
                members
        );
    }
}
