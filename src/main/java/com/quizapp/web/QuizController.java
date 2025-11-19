package com.quizapp.web;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.interfaces.SolvedQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class QuizController {

    private final GuestQuizService guestQuizService;
    private final SolvedQuizService solvedQuizService;

    @PostMapping("/quiz/category")
    public String createQuiz(@RequestParam("categoryId") Long categoryId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        int numberOfQuestions = 5;
        Long id;

        if (userDetails == null) {
            Quiz quiz = this.guestQuizService.createQuiz(categoryId, numberOfQuestions);
            id = quiz.getId();
        } else {
            SolvedQuiz solvedQuiz = this.solvedQuizService.createQuiz(categoryId, numberOfQuestions, userDetails.getUsername());
            id = solvedQuiz.getId();
        }

        redirectAttributes.addAttribute("page", 0);

        return "redirect:/quiz/" + id;
    }

    @GetMapping("/quiz/{quizId}")
    public ModelAndView showQuiz(@PathVariable Long quizId,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        ModelAndView modelAndView = new ModelAndView("quiz");

        if (userDetails == null) {
            Quiz quiz = this.guestQuizService.getSolvedQuizById(quizId);
            modelAndView.addObject("quiz", quiz);
        } else {
            SolvedQuizDTO solvedQuizDTO = this.solvedQuizService.getSolvedQuizById(quizId);
            modelAndView.addObject("quiz", solvedQuizDTO);
        }

        return modelAndView;
    }

    @PostMapping("/quiz/submit")
    public ModelAndView submitQuiz(@RequestParam("quizId") Long quizId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam Map<String, String> formData) {

        ModelAndView modelAndView = new ModelAndView("result");
        QuizResultDTO quizResultDTO;

        if (userDetails == null) {
            quizResultDTO = this.guestQuizService.evaluateQuiz(quizId, formData);
        } else {
            quizResultDTO = this.solvedQuizService.evaluateQuiz(quizId, formData, userDetails.getUsername());
        }

        modelAndView.addObject("result", quizResultDTO);

        return modelAndView;
    }

    @GetMapping("/solved-quiz/{id}")
    public ModelAndView showSolvedQuizById(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        SolvedQuizDTO solvedQuizDTO = this.solvedQuizService.getSolvedQuizById(id);
        modelAndView.addObject("solvedQuiz", solvedQuizDTO);

        return modelAndView;
    }
}