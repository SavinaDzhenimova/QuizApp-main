package com.quizapp.service;

import com.quizapp.exception.CategoryStatisticsNotFound;
import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.entity.CategoryStatistics;
import com.quizapp.repository.CategoryStatisticsRepository;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryStatisticsServiceImpl implements CategoryStatisticsService {

    private final CategoryStatisticsRepository categoryStatisticsRepository;
    private final CategoryService categoryService;

    @Override
    public List<CategoryStatsDTO> getAllCategoriesStatsForCharts() {
        return this.categoryStatisticsRepository.findAll().stream()
                .map(cat -> CategoryStatsDTO.builder()
                        .categoryId(cat.getCategoryId())
                        .categoryName(cat.getCategoryName())
                        .totalStartedQuizzes(cat.getTotalStartedQuizzes())
                        .totalCompletedQuizzes(cat.getTotalCompletedQuizzes())
                        .totalUnfinishedQuizzes(cat.getTotalStartedQuizzes() - cat.getTotalCompletedQuizzes())
                        .totalCorrectAnswers(cat.getTotalCorrectAnswers())
                        .totalQuestions(cat.getTotalQuestions())
                        .averageAccuracy(cat.getAverageAccuracy())
                        .completionRate(cat.getCompletionRate())
                        .build())
                .toList();
    }

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
                .totalCorrectAnswers(0)
                .totalQuestions(0)
                .averageAccuracy(0)
                .completionRate(0)
                .build();
    }

    @Override
    public void updateOnQuizCompleted(Long categoryId, int correctAnswers, int totalQuestions) {
        CategoryStatistics stats = categoryStatisticsRepository
                .findByCategoryId(categoryId)
                .orElseThrow(() -> new CategoryStatisticsNotFound("Не е намерена статистика за тази категория."));

        stats.setTotalCompletedQuizzes(stats.getTotalCompletedQuizzes() + 1);

        stats.setTotalQuestions(stats.getTotalQuestions() + totalQuestions);

        stats.setTotalCorrectAnswers(stats.getTotalCorrectAnswers() + correctAnswers);

        double newAverageAccuracy = (stats.getTotalCorrectAnswers() * 100.00) / stats.getTotalQuestions();
        stats.setAverageAccuracy(newAverageAccuracy);

        int started = stats.getTotalStartedQuizzes();
        if (started > 0) {
            double completionRate = (stats.getTotalCompletedQuizzes() * 100.00) / started;
            stats.setCompletionRate(completionRate);
        }

        this.categoryStatisticsRepository.saveAndFlush(stats);
    }
}