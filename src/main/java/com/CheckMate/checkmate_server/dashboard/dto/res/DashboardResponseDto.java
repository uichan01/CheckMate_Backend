package com.CheckMate.checkmate_server.dashboard.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DashboardResponseDto {
    // 제출해야할 과제 수(남은 과제 수)
    private int remainTaskCnt;
    // 가장 가까운 미팅
    private DashboardMeetingResponseDto closeMeeting;
    // 제출하지 않은 과제
    private List<DashboardTaskResponseDto> remainTasks;
    // 멤버 별 참여율
    private List<DashboardUserResponseDto> members;
}
