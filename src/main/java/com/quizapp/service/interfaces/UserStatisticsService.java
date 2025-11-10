package com.quizapp.service.interfaces;

import com.quizapp.model.entity.UserStatistics;

public interface UserStatisticsService {

    UserStatistics updateUserStatistics(UserStatistics userStatistics, long correctAnswers, int totalQuestions);
}