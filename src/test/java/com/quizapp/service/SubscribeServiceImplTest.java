package com.quizapp.service;

import com.quizapp.model.dto.SubscribeDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Subscription;
import com.quizapp.repository.SubscriptionRepository;
import com.quizapp.service.events.SubscribeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscribeServiceImplTest {

    @Mock
    private SubscriptionRepository mockSubscriptionRepository;
    @Mock
    private ApplicationEventPublisher mockAplEventPublisher;
    @InjectMocks
    private SubscribeServiceImpl mockSubscribeService;

    private SubscribeDTO mockSubscribeDTO;
    private Subscription mockSubscription;

    @BeforeEach
    void setUp() {
        this.mockSubscribeDTO = SubscribeDTO.builder()
                .email("subscribe@gmail.com")
                .build();

        this.mockSubscription = Subscription.builder()
                .email("subscribe@gmail.com")
                .build();
    }

    @Test
    void subscribe_ShouldReturnError_WhenDtoIsNull() {
        Result result = this.mockSubscribeService.subscribe(null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Невалидни входни данни!", result.getMessage());
        verifyNoInteractions(this.mockSubscriptionRepository);
        verifyNoInteractions(this.mockAplEventPublisher);
    }

    @Test
    void subscribe_ShouldReturnError_WhenSubscriptionEmailIsPresent() {
        when(this.mockSubscriptionRepository.findByEmail(this.mockSubscribeDTO.getEmail()))
                .thenReturn(Optional.of(this.mockSubscription));

        Result result = this.mockSubscribeService.subscribe(this.mockSubscribeDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Вече сте абониран за новостите в QuizApp.", result.getMessage());
        verify(this.mockSubscriptionRepository, never()).saveAndFlush(any());
        verify(this.mockAplEventPublisher, never()).publishEvent(any());
    }

    @Test
    void subscribe_ShouldSubscribeAndSendEmail_WhenEmailNoneExist() {
        when(this.mockSubscriptionRepository.findByEmail(this.mockSubscribeDTO.getEmail()))
                .thenReturn(Optional.empty());

        Result result = this.mockSubscribeService.subscribe(this.mockSubscribeDTO);
        ArgumentCaptor<SubscribeEvent> eventCaptor = ArgumentCaptor.forClass(SubscribeEvent.class);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно се абонирахте.", result.getMessage());
        verify(this.mockSubscriptionRepository, times(1)).saveAndFlush(this.mockSubscription);
        verify(this.mockAplEventPublisher).publishEvent(eventCaptor.capture());
        Assertions.assertEquals(this.mockSubscribeDTO.getEmail(), eventCaptor.getValue().getEmail());
    }
}