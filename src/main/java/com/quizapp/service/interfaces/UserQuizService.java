package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.SolvedQuiz;

import java.util.Map;

public interface UserQuizService {

    SolvedQuizDTO getSolvedQuizById(Long id);

    SolvedQuiz createQuiz(Long categoryId, int numberOfQuestions, String username);

    QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> answers);

    boolean deleteQuizById(Long id);
}