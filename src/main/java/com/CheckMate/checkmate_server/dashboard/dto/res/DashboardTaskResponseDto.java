package com.CheckMate.checkmate_server.dashboard.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DashboardTaskResponseDto {
    // 과제 id
    private Long taskId;
    // 과제 제목
    private String title;
    // 작성자명
    private String nickname;
}
