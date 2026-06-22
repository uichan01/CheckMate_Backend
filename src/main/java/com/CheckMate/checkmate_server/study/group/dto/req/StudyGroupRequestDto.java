package com.CheckMate.checkmate_server.study.group.dto.req;

import com.CheckMate.checkmate_server.study.group.domain.GroupJoinPolicy;
import com.CheckMate.checkmate_server.study.group.domain.GroupScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudyGroupRequestDto {
    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 50, message = "제목은 50자 이하여야 합니다.")
    private String title;

    @Size(max=255, message = "설명글은 255자 이하여야 합니다.")
    private String description;

    @NotNull(message = "공개 범위는 필수입니다.")
    private GroupScope scope;

    @NotNull(message = "입장 정책은 필수입니다.")
    private GroupJoinPolicy joinPolicy;
}