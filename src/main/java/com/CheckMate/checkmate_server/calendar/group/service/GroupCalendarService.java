package com.CheckMate.checkmate_server.calendar.group.service;

import com.CheckMate.checkmate_server.calendar.group.dto.res.GroupScheduleResponse;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GroupCalendarService {
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<GroupScheduleResponse> getGroupSchedules(
            Long studyId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDateTime startDateTime = startDate.atStartOfDay();

        LocalDateTime endDateTime = endDate
                .plusDays(1)
                .atStartOfDay();

        List<GroupScheduleResponse> taskSchedules = taskRepository
                .findAllByStudyGroupEntity_StudyIdAndDueDateGreaterThanEqualAndDueDateLessThan(
                        studyId,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(GroupScheduleResponse::fromTask)
                .toList();

        List<GroupScheduleResponse> meetingSchedules = meetingRepository
                .findAllByStudyGroupEntity_StudyIdAndMeetingDateGreaterThanEqualAndMeetingDateLessThan(
                        studyId,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(GroupScheduleResponse::fromMeeting)
                .toList();

        return Stream.concat(taskSchedules.stream(), meetingSchedules.stream())
                .sorted(Comparator.comparing(GroupScheduleResponse::getDate))
                .toList();
    }
}
