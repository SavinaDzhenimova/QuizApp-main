package com.quizapp.web.rest;

import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.service.interfaces.SolvedQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizRestController {

    private final SolvedQuizService solvedQuizService;

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestParam Long categoryId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(defaultValue = "5") int numberOfQuestions) {

        SolvedQuiz solvedQuiz = this.solvedQuizService.createQuiz(categoryId, numberOfQuestions, userDetails.getUsername());

        return ResponseEntity.ok(Objects.requireNonNullElseGet(solvedQuiz, () -> Map.of(
                "error", "Няма налични въпроси за категория с ID " + categoryId + ".")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        SolvedQuizDTO solvedQuizDTO = this.solvedQuizService.getSolvedQuizById(id);

        return ResponseEntity.ok(Objects.requireNonNullElseGet(solvedQuizDTO, () -> Map.of(
                "error", "Куизът с ID " + id + " не е намерен!")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuizById(@PathVariable Long id) {
        boolean isDeleted = this.solvedQuizService.deleteQuizById(id);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Куизът с ID " + id + " не е намерен, за да бъде премахнат."));
        }

        return ResponseEntity.noContent().build();
    }
}