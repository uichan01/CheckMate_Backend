package com.CheckMate.checkmate_server.study.task.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiFeedbackMessage {
    private Long feedbackId;
    private Long submissionId;
    private String taskTitle;
    private String taskContent;
    private String submissionTitle;
    private String submissionContent;
    private List<String> attachmentUrls;
    private int retryCount;

    public AiFeedbackMessage(Long feedbackId, Long submissionId, String taskTitle, String taskContent,
                             String submissionTitle, String submissionContent, List<String> attachmentUrls) {
        this(feedbackId, submissionId, taskTitle, taskContent, submissionTitle, submissionContent,
                attachmentUrls, 0);
    }

    public AiFeedbackMessage(Long feedbackId, Long submissionId, String taskTitle, String taskContent,
                             String submissionTitle, String submissionContent, List<String> attachmentUrls,
                             int retryCount) {
        this.feedbackId = feedbackId;
        this.submissionId = submissionId;
        this.taskTitle = taskTitle;
        this.taskContent = taskContent;
        this.submissionTitle = submissionTitle;
        this.submissionContent = submissionContent;
        this.attachmentUrls = attachmentUrls;
        this.retryCount = retryCount;
    }

    public AiFeedbackMessage nextRetry() {
        return new AiFeedbackMessage(feedbackId, submissionId, taskTitle, taskContent,
                submissionTitle, submissionContent, attachmentUrls, retryCount + 1);
    }
}
