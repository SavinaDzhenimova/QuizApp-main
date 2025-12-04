package com.quizapp.service.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AddedAdminEvent extends ApplicationEvent {

    private String username;

    private String email;

    private String tempPassword;

    public AddedAdminEvent(Object source, String username, String email, String tempPassword) {
        super(source);
        this.username = username;
        this.email = email;
        this.tempPassword = tempPassword;
    }
}