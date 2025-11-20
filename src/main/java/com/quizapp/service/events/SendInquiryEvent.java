package com.quizapp.service.events;

import org.springframework.context.ApplicationEvent;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}