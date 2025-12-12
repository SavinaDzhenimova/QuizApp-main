package com.quizapp.web;

import com.quizapp.model.dto.ReportProblemDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.ReportProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final ReportProblemService reportProblemService;

    @GetMapping
    public ModelAndView getReportProblemPage(Model model) {
        if (!model.containsAttribute("reportProblemDTO")) {
            model.addAttribute("reportProblemDTO", new ReportProblemDTO());
        }

        return new ModelAndView("report-problem");
    }

    @PostMapping("/send-report")
    public ModelAndView reportProblem(@Valid @ModelAttribute("reportProblemDTO") ReportProblemDTO reportProblemDTO,
                                      BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reportProblemDTO", reportProblemDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.reportProblemDTO",
                            bindingResult);

            return new ModelAndView("report-problem");
        }

        Result result = this.reportProblemService.sendEmailToReportProblem(reportProblemDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/report-problem");
    }
}