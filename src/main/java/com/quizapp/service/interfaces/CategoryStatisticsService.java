package com.quizapp.service.interfaces;

import com.quizapp.model.dto.category.CategoryStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryStatisticsService {

    Page<CategoryStatsDTO> getAllCategoriesFiltered(Long categoryId, Pageable pageable);

    void increaseStartedQuizzes(Long categoryId);

    void updateOnQuizCompleted(Long categoryId, int correctAnswers, int totalQuestions);
}