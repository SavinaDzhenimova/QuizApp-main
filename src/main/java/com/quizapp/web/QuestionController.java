package com.quizapp.web;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.service.interfaces.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        List<QuestionDTO> questionDTOs = this.questionService.getAllQuestions();
        return ResponseEntity.ok(questionDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        QuestionDTO questionDTO = this.questionService.getQuestionById(id);

        if (questionDTO == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Въпрос с ID " + id + " не е намерен."));
        }

        return ResponseEntity.ok(questionDTO);
    }

    @PostMapping
    public ResponseEntity<?> addQuestion(@RequestBody AddQuestionDTO addQuestionDTO) {
        Object result = this.questionService.addQuestion(addQuestionDTO);

        if (result instanceof Map) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestionById(@PathVariable Long id) {
        boolean isDeleted = this.questionService.deleteQuestionById(id);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Въпрос с ID " + id + " не е намерен, за да бъде премахнат."));
        }

        return ResponseEntity.noContent().build();
    }
}