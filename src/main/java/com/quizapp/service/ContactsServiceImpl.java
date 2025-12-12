package com.quizapp.service;

import com.quizapp.model.dto.AddInquiryDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.events.SendInquiryEvent;
import com.quizapp.service.interfaces.ContactsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactsServiceImpl implements ContactsService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Result sendInquiryEmail(AddInquiryDTO addInquiryDTO) {
        if (addInquiryDTO == null) {
            return new Result(false, "Невалидни входни данни.");
        }

        this.applicationEventPublisher.publishEvent(
                new SendInquiryEvent(this, addInquiryDTO.getFullName(), addInquiryDTO.getEmail(),
                        addInquiryDTO.getTheme(), addInquiryDTO.getMessage()));

        return new Result(true, "Вашето запитване беше изпратено успешно!");
    }
}