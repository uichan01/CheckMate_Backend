package com.CheckMate.checkmate_server.study.category.dto.res;

import com.CheckMate.checkmate_server.study.category.domain.StudyCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyCategoryResponseDto {
    private Long categoryId;
    private String categoryName;

    public static StudyCategoryResponseDto from(StudyCategoryEntity entity) {
        return new StudyCategoryResponseDto(entity.getCategoryId(), entity.getCategoryName());
    }
}
