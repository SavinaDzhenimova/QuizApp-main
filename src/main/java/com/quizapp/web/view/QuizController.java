package com.quizapp.web.view;

import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quiz/category")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

}