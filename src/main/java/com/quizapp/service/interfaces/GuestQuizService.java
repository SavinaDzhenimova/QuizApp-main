package com.quizapp.service.interfaces;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;

import java.time.LocalDateTime;
import java.util.Map;

public interface GuestQuizService {

    void evaluateQuiz(String quizId, Map<String, String> answers);

    QuizResultDTO getQuizResult(String viewToken);

    QuizDTO showQuizResult(String token);

    void deleteExpiredGuestQuizzes(LocalDateTime dateTime);
}