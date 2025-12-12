package com.quizapp.service;

import com.quizapp.model.dto.AddInquiryDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.events.SendInquiryEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactsServiceTest {

    @Mock
    private ApplicationEventPublisher mockAplEventPublisher;
    @InjectMocks
    private ContactsServiceImpl contactsService;

    private AddInquiryDTO addInquiryDTO;

    @BeforeEach
    void  setUp() {
        this.addInquiryDTO = AddInquiryDTO.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .theme("Question about quizzes")
                .message("How to start a quiz?")
                .build();
    }

    @Test
    void sendInquiryEmail_ShouldReturnError_WhenDtoNotValid() {
        Result result = this.contactsService.sendInquiryEmail(null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Невалидни входни данни.", result.getMessage());
    }

    @Test
    void sendInquiryEmail_ShouldSendInquiryEmail_WhenDtoIdValid() {
        Result result = this.contactsService.sendInquiryEmail(this.addInquiryDTO);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Вашето запитване беше изпратено успешно!", result.getMessage());
        verify(this.mockAplEventPublisher, times(1)).publishEvent(any(SendInquiryEvent.class));
    }
}