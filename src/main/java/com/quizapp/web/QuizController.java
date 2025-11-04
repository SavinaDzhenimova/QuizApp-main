package com.quizapp.web;

import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                "error", "Няма налични въпроси за категория с ID " + categoryId + ".")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        QuizDTO quizDTO = this.quizService.getQuizById(id);

        return ResponseEntity.ok(Objects.requireNonNullElseGet(quizDTO, () -> Map.of(
                "error", "Куизът с ID " + id + " не е намерен!")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuizById(@PathVariable Long id) {
        boolean isDeleted = this.quizService.deleteQuizById(id);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Куизът с ID " + id + " не е намерен, за да бъде премахнат."));
        }

        return ResponseEntity.noContent().build();
    }
}