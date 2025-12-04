package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeletionWarningEventListener {

    private final EmailService emailService;

    @EventListener
    public void sendInactiveUsersNotWarnedEmails(DeletionWarningEvent deletionWarningEvent) {
        this.emailService.sendInactiveUserNotWarnedEmail(deletionWarningEvent.getUsername(),
                deletionWarningEvent.getEmail());
    }
}