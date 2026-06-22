package com.CheckMate.checkmate_server.study.group.dto.res;

import com.CheckMate.checkmate_server.study.group.domain.GroupJoinPolicy;
import com.CheckMate.checkmate_server.study.group.domain.GroupScope;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyGroupResponseDto {
    private Long studyId;
    private String categoryName;
    private String title;
//    private String description;
//    private GroupScope scope;
//    private GroupJoinPolicy joinPolicy;
    public static StudyGroupResponseDto from(StudyGroupEntity entity) {
        return new StudyGroupResponseDto(
                entity.getStudyId(),
                entity.getCategoryEntity().getCategoryName(),
                entity.getTitle()
        );
    }
}
