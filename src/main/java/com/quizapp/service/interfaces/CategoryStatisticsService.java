package com.quizapp.service.interfaces;

public interface CategoryStatisticsService {

    void increaseStartedQuizzes(Long categoryId);

    void updateOnQuizCompleted(Long categoryId, double scorePercent, int correctAnswers, int totalQuestions);
}