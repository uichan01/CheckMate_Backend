package com.CheckMate.checkmate_server.dashboard.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DashboardUserResponseDto {
    // 유저ID
    private Long userId;
    // 닉네임
    private String nickname;
    // 제출 수
    private long submittedTaskCount;
}
