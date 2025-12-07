package com.quizapp.service.interfaces;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;

import java.util.Map;

public interface GuestQuizService {

    void evaluateQuiz(QuizSubmissionDTO quizSubmissionDTO);

    QuizResultDTO getQuizResult(String viewToken);

    QuizDTO showQuizResult(String token);

    String deleteExpiredGuestQuizzes();
}