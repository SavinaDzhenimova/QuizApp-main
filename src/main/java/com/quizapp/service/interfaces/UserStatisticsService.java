package com.quizapp.service.interfaces;

import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;

import java.time.LocalDateTime;
import java.util.List;

public interface UserStatisticsService {

    UserStatistics createInitialStatistics(User user);

    UserStatistics updateUserStatistics(UserStatistics userStatistics, long correctAnswers, int totalQuestions, LocalDateTime solvedAt);

    List<User> findInactiveUsers(LocalDateTime dateTime);
}