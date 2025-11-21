package com.quizapp.service;

import com.quizapp.model.dto.SubscribeDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Subscription;
import com.quizapp.repository.SubscriptionRepository;
import com.quizapp.service.events.SubscribeEvent;
import com.quizapp.service.interfaces.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscribeServiceImpl implements SubscribeService {

    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Result subscribe(SubscribeDTO subscribeDTO) {

        if (subscribeDTO == null) {
            return new Result(false, "Невалидни входни данни!");
        }

        Optional<Subscription> optionalSubscription = this.subscriptionRepository.findByEmail(subscribeDTO.getEmail());

        if (optionalSubscription.isPresent()) {
            return new Result(false, "Вече сте абониран за новостите в QuizApp.");
        }

        Subscription subscription = Subscription.builder()
                .email(subscribeDTO.getEmail())
                .build();

        this.subscriptionRepository.saveAndFlush(subscription);

        this.applicationEventPublisher.publishEvent(
                new SubscribeEvent(this, subscription.getEmail())
        );

        return new Result(true, "Успешно се абонирахте.");
    }
}