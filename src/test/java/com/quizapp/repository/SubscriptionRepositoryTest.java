package com.quizapp.repository;

import com.quizapp.model.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    @BeforeEach
    void setUp() {
        Subscription subscription = Subscription.builder()
                .email("user@gmail.com")
                .build();
        this.subscriptionRepo.save(subscription);
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotFound() {
        Optional<Subscription> optionalSubscription = this.subscriptionRepo.findByEmail("not_existing");

        assertThat(optionalSubscription).isEmpty();
    }

    @Test
    void findByEmail_ShouldReturnSubscription_WhenEmailFound() {
        Optional<Subscription> optionalSubscription = this.subscriptionRepo.findByEmail("user@gmail.com");

        assertThat(optionalSubscription).isPresent();
        assertThat(optionalSubscription.get().getEmail()).isEqualTo("user@gmail.com");
    }
}