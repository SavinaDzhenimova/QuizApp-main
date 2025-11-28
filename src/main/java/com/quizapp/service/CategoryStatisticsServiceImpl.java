package com.quizapp.service;

import com.quizapp.model.entity.CategoryStatistics;
import com.quizapp.repository.CategoryStatisticsRepository;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryStatisticsServiceImpl implements CategoryStatisticsService {

    private final CategoryStatisticsRepository categoryStatisticsRepository;
    private final CategoryService categoryService;

    @Override
    public void increaseStartedQuizzes(Long categoryId) {
        CategoryStatistics categoryStatistics = categoryStatisticsRepository.findByCategoryId(categoryId)
                .orElseGet(() -> this.createNewStatistics(categoryId));

        categoryStatistics.setTotalStartedQuizzes(categoryStatistics.getTotalStartedQuizzes() + 1);

        int completed = categoryStatistics.getTotalCompletedQuizzes();
        int started = categoryStatistics.getTotalStartedQuizzes();
        if (started > 0) {
            categoryStatistics.setCompletionRate((completed * 100.0) / started);
        }

        this.categoryStatisticsRepository.saveAndFlush(categoryStatistics);
    }

    private CategoryStatistics createNewStatistics(Long categoryId) {
        return CategoryStatistics.builder()
                .categoryId(categoryId)
                .categoryName(this.categoryService.getCategoryNameById(categoryId))
                .totalStartedQuizzes(0)
                .totalCompletedQuizzes(0)
                .averageScore(0)
                .averageAccuracy(0)
                .completionRate(0)
                .build();
    }

    @Override
    public void updateOnQuizCompleted(Long categoryId, double scorePercent, int correctAnswers, int totalQuestions) {
        CategoryStatistics stats = categoryStatisticsRepository
                .findByCategoryId(categoryId)
                .orElseGet(() -> this.createNewStatistics(categoryId));

        // Увеличаване на броя завършени куизове
        stats.setTotalCompletedQuizzes(stats.getTotalCompletedQuizzes() + 1);

        // Обновяване на среден резултат (averageScore)
        double oldAvgScore = stats.getAverageScore();
        int completed = stats.getTotalCompletedQuizzes();

        double newAvgScore = ((oldAvgScore * (completed - 1)) + scorePercent) / completed;
        stats.setAverageScore(newAvgScore);

        // Обновяване на averageAccuracy (точност на отговорите)
        double accuracy = (correctAnswers * 100.0) / totalQuestions;
        double oldAccuracy = stats.getAverageAccuracy();

        double newAccuracy = ((oldAccuracy * (completed - 1)) + accuracy) / completed;
        stats.setAverageAccuracy(newAccuracy);

        // Completion rate = completed / started
        int started = stats.getTotalStartedQuizzes();
        if (started > 0) {
            stats.setCompletionRate((completed * 100.0) / started);
        }

        this.categoryStatisticsRepository.saveAndFlush(stats);
    }
}