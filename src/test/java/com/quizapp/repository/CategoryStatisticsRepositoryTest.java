package com.quizapp.repository;

import com.quizapp.model.entity.CategoryStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CategoryStatisticsRepositoryTest {

    @Autowired
    private CategoryStatisticsRepository categoryStatsRepo;

    @BeforeEach
    void setUp() {
        CategoryStatistics categoryStats = CategoryStatistics.builder()
                .categoryId(1L)
                .categoryName("Maths")
                .totalStartedQuizzes(7)
                .totalCompletedQuizzes(5)
                .totalQuestions(25)
                .totalCorrectAnswers(23)
                .averageAccuracy(92.00)
                .completionRate(71.00)
                .build();

        this.categoryStatsRepo.save(categoryStats);
    }

    @Test
    void findByCategoryId_ShouldReturnCategoryStats_WhenCategoryFound() {
        Optional<CategoryStatistics> optionalCategoryStats = this.categoryStatsRepo.findByCategoryId(1L);

        assertThat(optionalCategoryStats).isPresent();
        assertThat(optionalCategoryStats.get().getCategoryId()).isEqualTo(1L);
    }

    @Test
    void findByCategoryId_ShouldReturnEmpty_WhenCategoryNotFound() {
        Optional<CategoryStatistics> optionalCategoryStats = this.categoryStatsRepo.findByCategoryId(5L);

        assertThat(optionalCategoryStats).isEmpty();
    }
}