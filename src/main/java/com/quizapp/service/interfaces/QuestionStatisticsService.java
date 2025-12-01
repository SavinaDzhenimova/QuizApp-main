package com.quizapp.service.interfaces;

public interface QuestionStatisticsService {
    void increaseUsedQuestion(Long questionId, String questionText);
}