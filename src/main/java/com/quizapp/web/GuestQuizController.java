package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.service.interfaces.GuestQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/guest/quizzes")
@RequiredArgsConstructor
public class GuestQuizController {

    private final GuestQuizService guestQuizService;

    @PostMapping("/{viewToken}/submit")
    public String submitQuiz(@PathVariable String viewToken, @RequestParam Map<String, String> formData) {
        this.guestQuizService.evaluateQuiz(viewToken, formData);

        return "redirect:/guest/quizzes/" + viewToken + "/result";
    }

    @GetMapping("/{token}/result")
    public ModelAndView showSolvedGuestQuizResult(@PathVariable String token) {
        ModelAndView modelAndView = new ModelAndView("result");

        QuizResultDTO quizResultDTO = this.guestQuizService.getQuizResult(token);

        modelAndView.addObject("result", quizResultDTO);

        return modelAndView;
    }

    @GetMapping("/{token}/review")
    public ModelAndView showSolvedGuestQuiz(@PathVariable String token) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        QuizDTO quizDTO = this.guestQuizService.showQuizResult(token);
        modelAndView.addObject("solvedQuiz", quizDTO);

        return modelAndView;
    }
}