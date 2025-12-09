package com.quizapp.service;

import com.quizapp.exception.CategoryStatisticsNotFound;
import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.entity.CategoryStatistics;
import com.quizapp.repository.CategoryStatisticsRepository;
import com.quizapp.service.interfaces.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryStatisticsServiceImplTest {

    @Mock
    private CategoryStatisticsRepository mockCategoryStatisticsRepository;
    @Mock
    private CategoryService mockCategoryService;
    @InjectMocks
    private CategoryStatisticsServiceImpl mockCategoryStatisticsService;

    private CategoryStatistics mockCategoryStats;

    @BeforeEach
    void setUp() {
        this.mockCategoryStats = CategoryStatistics.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Category")
                .totalQuestions(50)
                .totalCorrectAnswers(32)
                .totalStartedQuizzes(10)
                .totalCompletedQuizzes(7)
                .averageAccuracy(88.50)
                .completionRate(80.00)
                .build();
    }

    @Test
    void getAllCategoriesFiltered_ShouldReturnPageCategoryStatsDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryStatistics> page = new PageImpl<>(List.of(this.mockCategoryStats));

        ArgumentCaptor<Specification<CategoryStatistics>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);

        when(this.mockCategoryStatisticsRepository.findAll(ArgumentMatchers.<Specification<CategoryStatistics>>any(), eq(pageable))).thenReturn(page);

        Page<CategoryStatsDTO> result = this.mockCategoryStatisticsService.getAllCategoriesFiltered(1L, pageable);

        verify(this.mockCategoryStatisticsRepository, times(1)).findAll(specCaptor.capture(), eq(pageable));

        Assertions.assertNotNull(specCaptor.getValue());
        Assertions.assertEquals(1, result.getTotalElements());

        CategoryStatsDTO dto = result.getContent().get(0);

        Assertions.assertEquals(this.mockCategoryStats.getCategoryId(), dto.getCategoryId());
        Assertions.assertEquals(this.mockCategoryStats.getCategoryName(), dto.getCategoryName());
        Assertions.assertEquals(this.mockCategoryStats.getTotalStartedQuizzes(), dto.getTotalStartedQuizzes());
        Assertions.assertEquals(this.mockCategoryStats.getTotalCompletedQuizzes(), dto.getTotalCompletedQuizzes());
        Assertions.assertEquals(this.mockCategoryStats.getTotalCorrectAnswers(), dto.getTotalCorrectAnswers());
        Assertions.assertEquals(this.mockCategoryStats.getTotalQuestions(), dto.getTotalQuestions());
        Assertions.assertEquals(this.mockCategoryStats.getAverageAccuracy(), dto.getAverageAccuracy());
        Assertions.assertEquals(this.mockCategoryStats.getCompletionRate(), dto.getCompletionRate());
    }

    @Test
    void increaseStartedQuizzes_ShouldCreateNewStats_WhenNoneExist() {
        when(this.mockCategoryStatisticsRepository.findByCategoryId(5L)).thenReturn(Optional.empty());

        this.mockCategoryStatisticsService.increaseStartedQuizzes(5L);

        ArgumentCaptor<CategoryStatistics> statsCaptor = ArgumentCaptor.forClass(CategoryStatistics.class);

        verify(this.mockCategoryStatisticsRepository).saveAndFlush(statsCaptor.capture());
        CategoryStatistics savedStats = statsCaptor.getValue();

        Assertions.assertEquals(1, savedStats.getTotalStartedQuizzes());
        Assertions.assertEquals(0, savedStats.getTotalCompletedQuizzes());
        Assertions.assertEquals(0.0, savedStats.getCompletionRate());}

    @Test
    void increaseStartedQuizzes_ShouldIncreaseStartAndRecalculateRate_WhenStatsExist() {
        when(this.mockCategoryStatisticsRepository.findByCategoryId(1L)).thenReturn(Optional.of(this.mockCategoryStats));

        this.mockCategoryStatisticsService.increaseStartedQuizzes(1L);

        Assertions.assertEquals(11, this.mockCategoryStats.getTotalStartedQuizzes());

        double expectedRate = this.mockCategoryStats.getTotalCompletedQuizzes() * 100.0 / this.mockCategoryStats.getTotalStartedQuizzes();
        Assertions.assertEquals(expectedRate, this.mockCategoryStats.getCompletionRate());

        verify(this.mockCategoryStatisticsRepository, times(1)).saveAndFlush(this.mockCategoryStats);
    }

    @Test
    void updateOnQuizCompleted_ShouldThrowException_WhenCategoryStatisticsNotFound() {
        when(this.mockCategoryStatisticsRepository.findByCategoryId(5L)).thenReturn(Optional.empty());

        CategoryStatisticsNotFound exception = Assertions.assertThrows(CategoryStatisticsNotFound.class,
                () -> this.mockCategoryStatisticsService.updateOnQuizCompleted(5L, 3, 5));

        Assertions.assertEquals("Не е намерена статистика за тази категория.", exception.getMessage());
    }

    @Test
    void updateOnQuizCompleted_ShouldUpdateCategoryStats_WhenQuizCompleted() {
        when(this.mockCategoryStatisticsRepository.findByCategoryId(1L)).thenReturn(Optional.of(this.mockCategoryStats));

        this.mockCategoryStatisticsService.updateOnQuizCompleted(1L, 3, 5);

        Assertions.assertEquals(8, this.mockCategoryStats.getTotalCompletedQuizzes());
        Assertions.assertEquals(55, this.mockCategoryStats.getTotalQuestions());
        Assertions.assertEquals(35, this.mockCategoryStats.getTotalCorrectAnswers());

        double newAverageAccuracy = (this.mockCategoryStats.getTotalCorrectAnswers() * 100.00) / this.mockCategoryStats.getTotalQuestions();
        Assertions.assertEquals(newAverageAccuracy, this.mockCategoryStats.getAverageAccuracy());

        double completionRate = (this.mockCategoryStats.getTotalCompletedQuizzes() * 100.00) / this.mockCategoryStats.getTotalStartedQuizzes();
        Assertions.assertEquals(completionRate, this.mockCategoryStats.getCompletionRate());

        verify(this.mockCategoryStatisticsRepository, times(1)).saveAndFlush(this.mockCategoryStats);
    }
}