package com.CheckMate.checkmate_server.calendar.group.dto.res;

import com.CheckMate.checkmate_server.calendar.group.domain.GroupScheduleType;
import com.CheckMate.checkmate_server.study.meeting.domain.MeetingEntity;
import com.CheckMate.checkmate_server.study.task.domain.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GroupScheduleResponse {

    private Long scheduleId;

    private LocalDateTime date;

    private GroupScheduleType scheduleType;

    private String title;

    public static GroupScheduleResponse fromTask(TaskEntity taskEntity) {
        return new GroupScheduleResponse(
                taskEntity.getTaskId(),
                taskEntity.getDueDate(),
                GroupScheduleType.TASK,
                taskEntity.getTitle()
        );
    }

    public static GroupScheduleResponse fromMeeting(MeetingEntity meetingEntity) {
        return new GroupScheduleResponse(
                meetingEntity.getMeetingId(),
                meetingEntity.getMeetingDate(),
                GroupScheduleType.MEETING,
                meetingEntity.getTitle()
        );
    }
}
