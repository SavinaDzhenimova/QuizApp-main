package com.quizapp.web;

import com.quizapp.model.dto.question.AddQuestionDTO;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.question.QuestionPageDTO;
import com.quizapp.model.dto.question.UpdateQuestionDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ModelAndView showQuestions(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String questionText,
                                      @RequestParam(required = false) Long categoryId,
                                      Model model) {

        ModelAndView modelAndView = new ModelAndView("questions");

        if (!model.containsAttribute("updateQuestionDTO")) {
            model.addAttribute("updateQuestionDTO", new UpdateQuestionDTO());
        }

        String decodedText = "";
        if (questionText != null) {
            decodedText = URLDecoder.decode(questionText, StandardCharsets.UTF_8);
        }

        QuestionPageDTO<QuestionDTO> questionsPageDTO = this.questionService.getAllQuestions(decodedText, categoryId,
                PageRequest.of(page, size));

        modelAndView.addObject("questions", questionsPageDTO.getQuestions());
        modelAndView.addObject("currentPage", questionsPageDTO.getCurrentPage());
        modelAndView.addObject("totalPages", questionsPageDTO.getTotalPages());
        modelAndView.addObject("totalElements", questionsPageDTO.getTotalElements());
        modelAndView.addObject("size", questionsPageDTO.getSize());
        modelAndView.addObject("questionText", questionText);
        modelAndView.addObject("categoryId", categoryId);

        if (questionsPageDTO.getTotalElements() == 0) {
            modelAndView.addObject("warning", "Няма намерени въпроси за зададените критерии!");
        }

        return modelAndView;
    }

    @PutMapping("/update/{id}")
    public ModelAndView updateQuestion(@PathVariable Long id,
                                       @Valid @ModelAttribute("updateQuestionDTO") UpdateQuestionDTO updateQuestionDTO,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("updateQuestionDTO", updateQuestionDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.updateQuestionDTO",
                            bindingResult)
                    .addFlashAttribute("openModal", true);

            return new ModelAndView("redirect:/questions");
        }

        Result result = this.questionService.updateQuestion(id, updateQuestionDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/questions");
    }

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