package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.service.interfaces.UserQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/users/quizzes")
@RequiredArgsConstructor
public class UserQuizController {

    private final UserQuizService userQuizService;

    @PostMapping("/{viewToken}/submit")
    public String submitQuiz(@PathVariable String viewToken,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute QuizSubmissionDTO quizSubmissionDTO,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("quizSubmissionDTO", quizSubmissionDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.quizSubmissionDTO",
                            bindingResult);

            return "redirect:/quizzes/quiz/" + viewToken;
        }

        Long id = this.userQuizService.evaluateQuiz(quizSubmissionDTO, userDetails.getUsername());

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
    public ModelAndView showSolvedQuizById(@PathVariable Long id,
                                           @RequestParam(value = "from", defaultValue = "result") String from) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        QuizDTO solvedQuizDTO = this.userQuizService.getSolvedQuizById(id);
        modelAndView.addObject("solvedQuiz", solvedQuizDTO);

        return modelAndView;
    }
}