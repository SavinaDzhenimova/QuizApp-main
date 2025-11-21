package com.quizapp.service.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class SubscribeEvent extends ApplicationEvent {

    private String email;

    public SubscribeEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
}