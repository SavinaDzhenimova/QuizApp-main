package com.quizapp.web;

import com.quizapp.model.dto.AddInquiryDTO;
import com.quizapp.service.events.SendInquiryEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ContactsController {

    private final ApplicationEventPublisher applicationEventPublisher;

    @GetMapping("/contacts")
    public ModelAndView getContactsPage(Model model) {
        if (!model.containsAttribute("addInquiryDTO")) {
            model.addAttribute("addInquiryDTO", new AddInquiryDTO());
        }

        return new ModelAndView("contacts");
    }

    @PostMapping("/contacts/send-inquiry")
    public ModelAndView sendInquiry(@Valid @ModelAttribute("addInquiryDTO") AddInquiryDTO addInquiryDTO,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addInquiryDTO", addInquiryDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.addInquiryDTO",
                            bindingResult);

            return new ModelAndView("contacts");
        }

        this.applicationEventPublisher.publishEvent(
                new SendInquiryEvent(this, addInquiryDTO.getFullName(), addInquiryDTO.getEmail(),
                        addInquiryDTO.getTheme(), addInquiryDTO.getMessage()));

        redirectAttributes.addFlashAttribute("successMessage",
                "Вашето запитване беше изпратено успешно!");

        return new ModelAndView("redirect:/contacts");
    }
}