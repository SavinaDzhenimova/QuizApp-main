package com.quizapp.service.interfaces;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;

import java.time.LocalDateTime;
import java.util.Map;

public interface GuestQuizService {

    Quiz createQuiz(Long categoryId, int numberOfQuestions);

    Quiz getQuizFromTemp(String viewToken);

    void evaluateQuiz(String quizId, Map<String, String> answers);

    QuizResultDTO getQuizResult(String viewToken);

    QuizDTO showQuizResult(String token);

    void deleteExpiredGuestQuizzes(LocalDateTime dateTime);
}