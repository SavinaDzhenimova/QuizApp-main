package com.quizapp.web.view;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.QuestionService;
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
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/add-question")
    public ModelAndView showAddQuestionPage(Model model) {
        if (!model.containsAttribute("addQuestionDTO")) {
            model.addAttribute("addQuestionDTO", new AddQuestionDTO());
        }

        return new ModelAndView("add-question");
    }

    @PostMapping("/add-question")
    public ModelAndView addQuestion(@Valid @ModelAttribute("addQuestionDTO") AddQuestionDTO addQuestionDTO,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("addQuestionDTO", addQuestionDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.addQuestionDTO",
                            bindingResult);

            return new ModelAndView("add-question");
        }

        Result result = this.questionService.addQuestion(addQuestionDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/questions/add-question");
    }
}