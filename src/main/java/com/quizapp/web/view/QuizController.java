package com.quizapp.web.view;

import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/quiz/category")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ModelAndView createQuiz(@RequestParam("categoryId") Long categoryId) {
        ModelAndView modelAndView = new ModelAndView("quiz");
        int numberOfQuestions = 5;

        Quiz quiz = this.quizService.createQuiz(categoryId, numberOfQuestions);
        QuizDTO quizDTO = this.quizService.mapQuizToDTO(quiz, categoryId);

        modelAndView.addObject("quiz", quizDTO);

        return modelAndView;
    }
}