package com.CheckMate.checkmate_server.study.group.dto.res;

import com.CheckMate.checkmate_server.study.group.domain.GroupJoinPolicy;
import com.CheckMate.checkmate_server.study.group.domain.GroupScope;
import com.CheckMate.checkmate_server.study.group.domain.StudyGroupEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupResponseDto {
    private Long studyId;
    private String categoryName;
    private String title;
    private String description;
    private long participationCnt;
//    private GroupScope scope;
//    private GroupJoinPolicy joinPolicy;
    public static StudyGroupResponseDto from(StudyGroupEntity entity, long participationCnt) {
        return StudyGroupResponseDto.builder()
                .studyId(entity.getStudyId())
                .categoryName(entity.getCategoryEntity().getCategoryName())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .participationCnt(participationCnt)
                .build();
    }
}
