package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddInquiryDTO;
import com.quizapp.model.entity.Result;

public interface ContactsService {

    Result sendInquiryEmail(AddInquiryDTO addInquiryDTO);
}