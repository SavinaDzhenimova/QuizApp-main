package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.Quiz;

import java.util.Map;

public interface GuestQuizService {

    Quiz getSolvedQuizByViewToken(String viewToken);

    Quiz createQuiz(Long categoryId, int numberOfQuestions);

    QuizResultDTO evaluateQuiz(String quizId, Map<String, String> answers);

    SolvedQuizDTO showSolvedQuizResult(String token);
}