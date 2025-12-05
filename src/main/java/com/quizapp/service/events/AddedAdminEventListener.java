package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddedAdminEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleAddedAdminEvent(AddedAdminEvent addedAdminEvent) {
        this.emailService.sendAddedAdminEmail(addedAdminEvent.getUsername(), addedAdminEvent.getEmail());
    }
}