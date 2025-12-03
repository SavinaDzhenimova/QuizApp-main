package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InactiveUserEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleInactiveUserEvent(InactiveUserEvent inactiveUserEvent) {

        this.emailService.sendInactiveUserEmail(inactiveUserEvent.getUsername(), inactiveUserEvent.getEmail());
    }
}