package com.quizapp.service.interfaces;

import com.quizapp.model.entity.Quiz;

public interface QuizService {
    Quiz createQuiz(Long categoryId, int numberOfQuestions);
}
