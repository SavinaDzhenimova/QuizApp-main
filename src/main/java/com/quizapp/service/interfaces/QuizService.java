package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.entity.Quiz;

import java.util.Map;

public interface QuizService {
    QuizDTO getQuizById(Long id);

    Quiz createQuiz(Long categoryId, int numberOfQuestions);

    QuizDTO mapQuizToDTO(Long quizId, Long categoryId);

    boolean deleteQuizById(Long id);

    QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> answers);
}