package com.CheckMate.checkmate_server.study.task.ai.dto;

import org.springframework.context.ApplicationEvent;

public class AiFeedbackEvent extends ApplicationEvent {

    private final AiFeedbackMessage message;

    public AiFeedbackEvent(Object source, AiFeedbackMessage message) {
        super(source);
        this.message = message;
    }

    public AiFeedbackMessage getMessage() {
        return message;
    }
}
