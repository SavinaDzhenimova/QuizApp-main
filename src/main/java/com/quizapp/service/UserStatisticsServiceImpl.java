package com.quizapp.service;

import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.UserStatisticsRepository;
import com.quizapp.service.interfaces.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;

    public UserStatistics getUserStatistics(User user) {
        long totalCorrectAnswers = user.getSolvedQuizzes().stream()
                .mapToInt(SolvedQuiz::getScore)
                .count();

        double averageScore = user.getSolvedQuizzes().stream()
                .mapToDouble(solvedQuiz -> (double) solvedQuiz.getScore() / solvedQuiz.getMaxScore() * 100)
                .average()
                .orElse(0);

        long maxScore = user.getSolvedQuizzes().stream()
                .mapToInt(SolvedQuiz::getMaxScore)
                .count();

        return UserStatistics.builder()
                .user(user)
                .totalQuizzes(user.getSolvedQuizzes().size())
                .totalCorrectAnswers((int) totalCorrectAnswers)
                .maxScore((int) maxScore)
                .averageScore(averageScore)
                .build();
    }
}