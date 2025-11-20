package com.quizapp.web;

import com.quizapp.model.dto.ReportProblemDTO;
import com.quizapp.service.events.ReportProblemEvent;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/report-problem")
@RequiredArgsConstructor
public class ReportProblemController {

    private final ApplicationEventPublisher applicationEventPublisher;

    @GetMapping
    public ModelAndView getReportBugPage(Model model) {
        if (!model.containsAttribute("reportProblemDTO")) {
            model.addAttribute("reportProblemDTO", new ReportProblemDTO());
        }

        return new ModelAndView("report");
    }

    @PostMapping
    public ModelAndView reportProblem(@Valid @ModelAttribute("reportProblemDTO") ReportProblemDTO reportProblemDTO,
                                      BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reportProblemDTO", reportProblemDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.reportProblemDTO",
                            bindingResult);

            return new ModelAndView("report");
        }

        this.applicationEventPublisher.publishEvent(
                new ReportProblemEvent(this, reportProblemDTO.getFullName(), reportProblemDTO.getEmail(),
                        reportProblemDTO.getProblemType(), reportProblemDTO.getQuestionIdentifier(),
                        reportProblemDTO.getDescription()));

        redirectAttributes.addFlashAttribute("success", "Вашето запитване беше изпратено успешно!");

        return new ModelAndView("redirect:/contacts");
    }
}