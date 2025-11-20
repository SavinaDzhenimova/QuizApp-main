package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisterEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleUserRegisterEvent(UserRegisterEvent userRegisterEvent) {

        this.emailService.sendUserRegisterEmail(userRegisterEvent.getUsername(), userRegisterEvent.getEmail());
    }
}