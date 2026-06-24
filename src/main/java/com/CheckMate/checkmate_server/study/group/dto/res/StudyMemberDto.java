package com.CheckMate.checkmate_server.study.group.dto.res;

import com.CheckMate.checkmate_server.study.group.domain.StudyMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudyMemberDto {
    private Long userId;
    private String nickName;
    private StudyMemberRole role;
}

