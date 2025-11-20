package com.quizapp.web;

import com.quizapp.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class ContactsController {

    private final EmailService emailService;

    @GetMapping("/contacts")
    public ModelAndView getContactsPage() {
        return new ModelAndView("contacts");
    }

    @PostMapping("/contacts")
    public ModelAndView sendInquiry() {
        
    }
}