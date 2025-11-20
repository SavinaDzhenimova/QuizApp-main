package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;

@RequiredArgsConstructor
public class SendInquiryEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleSubscribeEvent(SendInquiryEvent sendInquiryEvent) {

        this.emailService.sendInquiryEmail(sendInquiryEvent.getFullName(), sendInquiryEvent.getEmail(),
                sendInquiryEvent.getTheme(), sendInquiryEvent.getMessage());
    }
}