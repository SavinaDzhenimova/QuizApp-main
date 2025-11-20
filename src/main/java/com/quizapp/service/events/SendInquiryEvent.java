package com.quizapp.service.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class SendInquiryEvent extends ApplicationEvent {

    private String fullName;

    private String email;

    private String theme;

    private String message;

    public SendInquiryEvent(Object source, String fullName, String email, String theme, String message) {
        super(source);
        this.fullName = fullName;
        this.email = email;
        this.theme = theme;
        this.message = message;
    }
}