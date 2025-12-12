package com.quizapp.web;

import com.quizapp.model.dto.AddInquiryDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.events.SendInquiryEvent;
import com.quizapp.service.interfaces.ContactsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactsController {

    private final ContactsService contactsService;

    @GetMapping
    public ModelAndView getContactsPage(Model model) {
        if (!model.containsAttribute("addInquiryDTO")) {
            model.addAttribute("addInquiryDTO", new AddInquiryDTO());
        }

        return new ModelAndView("contacts");
    }

    @PostMapping("/send-inquiry")
    public ModelAndView sendInquiry(@Valid @ModelAttribute("addInquiryDTO") AddInquiryDTO addInquiryDTO,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addInquiryDTO", addInquiryDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.addInquiryDTO",
                            bindingResult);

            return new ModelAndView("contacts");
        }

        Result result = this.contactsService.sendInquiryEmail(addInquiryDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/contacts");
    }
}