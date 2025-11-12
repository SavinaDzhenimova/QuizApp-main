package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.SolvedQuiz;
import jakarta.transaction.Transactional;

import java.util.Map;

public interface SolvedQuizService {
    SolvedQuizDTO getSolvedQuizById(Long id);

    SolvedQuiz createQuiz(Long categoryId, int numberOfQuestions, String username);

    @Transactional
    QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> formData, String username);

    boolean deleteQuizById(Long id);
}
