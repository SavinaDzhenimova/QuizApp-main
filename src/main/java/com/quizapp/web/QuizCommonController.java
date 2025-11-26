package com.quizapp.web;

import com.quizapp.model.entity.Quiz;
import com.quizapp.service.QuizCommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizCommonController {

    private final QuizCommonService quizCommonService;

    @PostMapping("/start")
    public String createQuiz(@RequestParam("categoryId") Long categoryId,
                             RedirectAttributes redirectAttributes) {
        int numberOfQuestions = 5;

        Quiz quiz = this.quizCommonService.createQuiz(categoryId, numberOfQuestions);

        redirectAttributes.addAttribute("page", 0);

        return "redirect:/quizzes/quiz/" + quiz.getViewToken();
    }

    @GetMapping("/quiz/{viewToken}")
    public ModelAndView showQuiz(@PathVariable String viewToken,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        ModelAndView modelAndView = new ModelAndView("quiz");

        Quiz quiz = this.quizCommonService.getQuizFromTemp(viewToken);
        modelAndView.addObject("quiz", quiz);
        modelAndView.addObject("isLogged", userDetails != null);

        return modelAndView;
    }
}