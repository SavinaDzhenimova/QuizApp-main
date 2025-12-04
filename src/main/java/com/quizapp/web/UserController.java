package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.service.interfaces.UserQuizService;
import com.quizapp.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserQuizService solvedQuizService;

    @GetMapping("/home")
    public ModelAndView showHomePage(@AuthenticationPrincipal UserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("home");

        UserDTO userDTO = this.userService.getUserInfo(userDetails.getUsername());

        modelAndView.addObject("user", userDTO);
        modelAndView.addObject("userStats", userDTO.getUserStats());

        if (userDTO.getSolvedQuizzes().isEmpty()) {
            modelAndView.addObject("warning", "Все още нямате решени куизове.");
        }

        return modelAndView;
    }

    @GetMapping("/quizzes")
    public ModelAndView viewUserQuizzes(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(defaultValue = "0") int page) {

        int pageSize = 5;
        Page<QuizDTO> quizPage = this.solvedQuizService
                .getSolvedQuizzesByUsername(userDetails.getUsername(), page, pageSize);

        ModelAndView modelAndView = new ModelAndView("quizzes");

        if (quizPage.isEmpty()) {
            modelAndView.addObject("warning", "Все още нямате решени куизове.");
        } else {
            modelAndView.addObject("quizzes", quizPage.getContent());
            modelAndView.addObject("currentPage", page);
            modelAndView.addObject("totalPages", quizPage.getTotalPages());
        }

        return modelAndView;
    }
}