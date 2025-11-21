package com.quizapp.service.events;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscribeEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleSubscription(SubscribeEvent subscribeEvent) {

         this.emailService.sendSubscribeEmail(subscribeEvent.getEmail());
    }
}