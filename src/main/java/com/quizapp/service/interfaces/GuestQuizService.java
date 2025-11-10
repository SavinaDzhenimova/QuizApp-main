package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.entity.SolvedQuiz;

import java.util.Map;

public interface GuestQuizService {

    Quiz getSolvedQuizById(Long id);

    Quiz createQuiz(Long categoryId, int numberOfQuestions);

    QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> answers);
}