package com.quizapp.web.view;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private Long CATEGORY_ID;

    @PostMapping("/category")
    public String createQuiz(@RequestParam("categoryId") Long categoryId, RedirectAttributes redirectAttributes) {
        CATEGORY_ID = categoryId;

        int numberOfQuestions = 5;
        Quiz quiz = quizService.createQuiz(categoryId, numberOfQuestions);

        redirectAttributes.addAttribute("page", 0);

        return "redirect:/quiz/" + quiz.getId();
    }

    @GetMapping("/{quizId}")
    public ModelAndView showQuiz(@PathVariable Long quizId) {

        ModelAndView modelAndView = new ModelAndView("quiz");

        QuizDTO quizDTO = quizService.mapQuizToDTO(quizId, CATEGORY_ID);

        modelAndView.addObject("quiz", quizDTO);

        return modelAndView;
    }
}