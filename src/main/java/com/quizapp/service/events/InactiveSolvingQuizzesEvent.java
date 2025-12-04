package com.quizapp.service.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class InactiveSolvingQuizzesEvent extends ApplicationEvent {

    private String username;

    private String email;

    public InactiveSolvingQuizzesEvent(Object source, String username, String email) {
        super(source);
        this.username = username;
        this.email = email;
    }
}