package com.quizapp.web.view;

import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.service.interfaces.SolvedQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/solved-quiz")
@RequiredArgsConstructor
public class SolvedQuizController {

    private final SolvedQuizService solvedQuizService;

    @GetMapping("/{id}")
    public ModelAndView showSolvedQuizById(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("solved-quiz");

        SolvedQuizDTO solvedQuizDTO = this.solvedQuizService.getSolvedQuizById(id);
        modelAndView.addObject("solvedQuiz", solvedQuizDTO);

        return modelAndView;
    }
}