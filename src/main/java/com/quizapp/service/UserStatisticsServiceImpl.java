package com.quizapp.service;

import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.UserStatisticsRepository;
import com.quizapp.service.interfaces.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;

    @Override
    public UserStatistics updateUserStatistics(UserStatistics userStatistics, long correctAnswers,
                                               int totalQuestions, LocalDateTime solvedAt) {
        userStatistics.setTotalQuizzes(userStatistics.getTotalQuizzes() + 1);
        userStatistics.setTotalCorrectAnswers(userStatistics.getTotalCorrectAnswers() + (int) correctAnswers);
        userStatistics.setMaxScore(userStatistics.getMaxScore() + totalQuestions);
        userStatistics.setAverageScore((double) userStatistics.getTotalCorrectAnswers() / userStatistics.getMaxScore() * 100);
        userStatistics.setLastSolvedAt(solvedAt);

        return this.userStatisticsRepository.saveAndFlush(userStatistics);
    }

    @Override
    public List<User> findInactiveUsers(LocalDateTime dateTime) {
        return this.userStatisticsRepository.findUsersInactiveSince(dateTime);
    }
}