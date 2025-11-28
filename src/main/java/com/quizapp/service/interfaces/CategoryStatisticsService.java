package com.quizapp.service.interfaces;

import com.quizapp.model.dto.category.CategoryStatsDTO;

import java.util.List;

public interface CategoryStatisticsService {

    List<CategoryStatsDTO> getAllCategoriesStatsForCharts();

    void increaseStartedQuizzes(Long categoryId);

    void updateOnQuizCompleted(Long categoryId, int correctAnswers, int totalQuestions);
}