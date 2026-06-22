package com.CheckMate.checkmate_server.study.group.dto.res;

import com.CheckMate.checkmate_server.study.group.domain.GroupJoinPolicy;
import com.CheckMate.checkmate_server.study.group.domain.GroupScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StudyGroupResponseDto {
    private Long groupId;
    private Long categoryId;
    private String title;
//    private String description;
//    private GroupScope scope;
//    private GroupJoinPolicy joinPolicy;
}
