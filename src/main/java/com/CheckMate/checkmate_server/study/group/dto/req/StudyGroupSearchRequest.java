package com.CheckMate.checkmate_server.study.group.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyGroupSearchRequest {
    private String keyword;
    private Long categoryId;
}
