package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForgotPasswordEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleForgotPasswordEvent(ForgotPasswordEvent forgotPasswordEvent) {

        this.emailService.sendForgotPasswordEmail(forgotPasswordEvent.getUsername(), forgotPasswordEvent.getEmail(),
                forgotPasswordEvent.getToken());
    }
}