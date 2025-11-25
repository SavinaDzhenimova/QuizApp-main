package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.UserQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/users/quizzes")
@RequiredArgsConstructor
public class UserQuizController {

    private final UserQuizService userQuizService;

    @PostMapping("/start")
    public String createQuiz(@RequestParam("categoryId") Long categoryId,
                             RedirectAttributes redirectAttributes) {
        int numberOfQuestions = 5;

        Quiz quiz = this.userQuizService.createQuiz(categoryId, numberOfQuestions);

        redirectAttributes.addAttribute("page", 0);

        return "redirect:/users/quizzes/" + quiz.getViewToken();
    }

    @GetMapping("/{viewToken}")
    public ModelAndView showQuiz(@PathVariable String viewToken) {

        ModelAndView modelAndView = new ModelAndView("quiz");

        Quiz quiz = this.userQuizService.getQuizFromTemp(viewToken);
        modelAndView.addObject("quiz", quiz);
        modelAndView.addObject("isLogged", true);

        return modelAndView;
    }

    @PostMapping("/{viewToken}/submit")
    public String submitQuiz(@PathVariable String viewToken,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam Map<String, String> formData) {
        Long id = this.userQuizService.evaluateQuiz(viewToken, formData, userDetails.getUsername());

        return "redirect:/users/quizzes/" + id + "/result";
    }

    @GetMapping("/{id}/result")
    public ModelAndView showSolvedQuizResult(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("result");

        QuizResultDTO quizResultDTO = this.userQuizService.getQuizResult(id);

        modelAndView.addObject("result", quizResultDTO);

        return modelAndView;
    }

    @GetMapping("/{id}/review")
    public ModelAndView showSolvedQuizById(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        QuizDTO solvedQuizDTO = this.userQuizService.getSolvedQuizById(id);
        modelAndView.addObject("solvedQuiz", solvedQuizDTO);

        return modelAndView;
    }
}