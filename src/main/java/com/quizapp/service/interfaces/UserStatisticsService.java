package com.quizapp.service.interfaces;

import com.quizapp.model.entity.UserStatistics;

import java.time.LocalDateTime;

public interface UserStatisticsService {

    UserStatistics updateUserStatistics(UserStatistics userStatistics, long correctAnswers, int totalQuestions, LocalDateTime solvedAt);
}