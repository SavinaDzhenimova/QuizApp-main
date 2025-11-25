package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.SolvedQuiz;
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

    private final UserQuizService solvedQuizService;

    @PostMapping("/start")
    public String createQuiz(@RequestParam("categoryId") Long categoryId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        int numberOfQuestions = 5;

        SolvedQuiz solvedQuiz = this.solvedQuizService.createQuiz(categoryId, numberOfQuestions, userDetails.getUsername());
        Long id = solvedQuiz.getId();

        redirectAttributes.addAttribute("page", 0);

        return "redirect:/users/quizzes/" + id;
    }

    @GetMapping("/{quizId}")
    public ModelAndView showQuiz(@PathVariable Long quizId) {

        ModelAndView modelAndView = new ModelAndView("quiz");

        QuizDTO solvedQuizDTO = this.solvedQuizService.getSolvedQuizById(quizId);
        modelAndView.addObject("quiz", solvedQuizDTO);
        modelAndView.addObject("isLogged", true);

        return modelAndView;
    }

    @PostMapping("/{id}/submit")
    public String submitQuiz(@PathVariable Long quizId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam Map<String, String> formData) {
        this.solvedQuizService.evaluateQuiz(quizId, formData, userDetails.getUsername());

        return "redirect:/users/quizzes/" + quizId + "/result";
    }

    @GetMapping("/{id}/result")
    public ModelAndView showSolvedQuizResult(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("result");

        QuizResultDTO quizResultDTO = this.solvedQuizService.getQuizResult(id);

        modelAndView.addObject("result", quizResultDTO);

        return modelAndView;
    }

    @GetMapping("/{id}/review")
    public ModelAndView showSolvedQuizById(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        QuizDTO solvedQuizDTO = this.solvedQuizService.getSolvedQuizById(id);
        modelAndView.addObject("solvedQuiz", solvedQuizDTO);

        return modelAndView;
    }
}