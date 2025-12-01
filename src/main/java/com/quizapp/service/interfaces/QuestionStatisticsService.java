package com.quizapp.service.interfaces;

import com.quizapp.model.entity.Quiz;

import java.util.Map;

public interface QuestionStatisticsService {

    void increaseUsedQuestion(Long questionId, String questionText);

    void updateOnQuizCompleted(Quiz quiz, Map<Long, String> userAnswers);
}