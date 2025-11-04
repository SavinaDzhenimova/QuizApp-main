package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.Quiz;

public interface QuizService {
    QuizDTO getQuizById(Long id);

    Quiz createQuiz(Long categoryId, int numberOfQuestions);

    boolean deleteQuizById(Long id);
}
