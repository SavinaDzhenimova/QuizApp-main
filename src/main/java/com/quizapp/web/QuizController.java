package com.quizapp.web;

import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestParam Long categoryId,
                                           @RequestParam(name = "numberOfQuestions", defaultValue = "20") int numberOfQuestions) {

        Quiz quiz = this.quizService.createQuiz(categoryId, numberOfQuestions);

        return ResponseEntity.ok(Objects.requireNonNullElseGet(quiz, () -> Map.of(
                "error", "Няма налични въпроси за категория с ID " + categoryId + "."
        )));
    }

}