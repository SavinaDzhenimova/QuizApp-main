package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.QuizCommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
                                 @AuthenticationPrincipal UserDetailsDTO userDetailsDTO,
                                 Model model) {

        if (!model.containsAttribute("quizSubmissionDTO")) {
            QuizSubmissionDTO quizSubmissionDTO = new QuizSubmissionDTO();
            quizSubmissionDTO.setViewToken(viewToken);
            model.addAttribute("quizSubmissionDTO", quizSubmissionDTO);
        }

        ModelAndView modelAndView = new ModelAndView("quiz");

        Quiz quiz = this.quizCommonService.getQuizFromTemp(viewToken);
        modelAndView.addObject("quiz", quiz);
        modelAndView.addObject("isLogged", userDetailsDTO != null);

        return modelAndView;
    }
}