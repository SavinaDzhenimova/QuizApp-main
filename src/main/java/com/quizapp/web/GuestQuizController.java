package com.quizapp.web;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.GuestQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/guest/quiz")
@RequiredArgsConstructor
public class GuestQuizController {

    private final GuestQuizService guestQuizService;

    @PostMapping("/category")
    public String createQuiz(@RequestParam("categoryId") Long categoryId,
                             RedirectAttributes redirectAttributes) {

        int numberOfQuestions = 5;

        Quiz quiz = this.guestQuizService.createQuiz(categoryId, numberOfQuestions);
        String viewToken = quiz.getViewToken();

        redirectAttributes.addAttribute("page", 0);

        return "redirect:/guest/quiz/" + viewToken;
    }

    @GetMapping("/{viewToken}")
    public ModelAndView showQuiz(@PathVariable String viewToken) {
        ModelAndView modelAndView = new ModelAndView("quiz");

        Quiz quiz = this.guestQuizService.getSolvedQuizByViewToken(viewToken);
        modelAndView.addObject("quiz", quiz);
        modelAndView.addObject("isLogged", false);

        return modelAndView;
    }

    @PostMapping("/submit")
    public ModelAndView submitQuiz(@RequestParam(value = "viewToken", required = false) String viewToken,
                                   @RequestParam Map<String, String> formData) {

        ModelAndView modelAndView = new ModelAndView("result");

        QuizResultDTO quizResultDTO = this.guestQuizService.evaluateQuiz(viewToken, formData);

        modelAndView.addObject("result", quizResultDTO);

        return modelAndView;
    }

    @GetMapping("/result/{token}")
    public ModelAndView showSolvedGuestQuiz(@PathVariable String token) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        QuizDTO solvedQuizDTO = this.guestQuizService.showQuizResult(token);
        modelAndView.addObject("solvedQuiz", solvedQuizDTO);

        return modelAndView;
    }
}