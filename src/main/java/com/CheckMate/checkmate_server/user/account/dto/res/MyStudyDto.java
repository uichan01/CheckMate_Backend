package com.CheckMate.checkmate_server.user.account.dto.res;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyStudyDto {

    private long studyId;
    private String studyName;
    private LocalDateTime joinedAt;

    public static MyStudyDto from(StudyMemberEntity studyMemberEntity) {
        return MyStudyDto.builder()
                .studyId(studyMemberEntity.getStudyGroupEntity().getStudyId())
                .studyName(studyMemberEntity.getStudyGroupEntity().getTitle())
                .joinedAt(studyMemberEntity.getCreatedAt())
                .build();
    }
}
