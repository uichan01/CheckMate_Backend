package com.CheckMate.checkmate_server.dashboard.service;

import com.CheckMate.checkmate_server.study.group.repository.StudyGroupRepository;
import com.CheckMate.checkmate_server.study.meeting.repository.MeetingRepository;
import com.CheckMate.checkmate_server.study.task.repository.TaskRepository;
import com.CheckMate.checkmate_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;

}
