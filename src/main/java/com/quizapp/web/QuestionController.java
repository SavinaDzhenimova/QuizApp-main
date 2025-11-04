package com.quizapp.web;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}