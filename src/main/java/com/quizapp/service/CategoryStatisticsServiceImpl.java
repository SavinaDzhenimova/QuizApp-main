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
    public void increaseStarted(Long categoryId) {
        CategoryStatistics categoryStatistics = categoryStatisticsRepository.findByCategoryId(categoryId)
                .orElseGet(() -> this.createNewStatistics(categoryId));

        categoryStatistics.setTotalStartedQuizzes(categoryStatistics.getTotalStartedQuizzes() + 1);
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
}