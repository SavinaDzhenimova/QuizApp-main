package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.enums.CategorySortField;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import com.quizapp.service.interfaces.UserStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatisticsController.class)
@Import(SecurityConfig.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryStatisticsService categoryStatsService;

    @MockitoBean
    private QuestionStatisticsService questionStatsService;

    @MockitoBean
    private UserStatisticsService userStatsService;

    @MockitoBean
    private GlobalController globalController;

    private CategoryStatsDTO categoryStatsDTO;

    @BeforeEach
    void setUp() {
        this.categoryStatsDTO = CategoryStatsDTO.builder()
                .categoryId(1L)
                .categoryName("Maths")
                .totalStartedQuizzes(10)
                .totalCompletedQuizzes(7)
                .totalUnfinishedQuizzes(3)
                .completionRate(70.00)
                .totalQuestions(35)
                .totalCorrectAnswers(33)
                .averageAccuracy(94.00)
                .build();
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showCategoriesStats_ShouldReturnPageCategoryStats_WhenDataFound() throws Exception {
        Page<CategoryStatsDTO> page = new PageImpl<>(List.of(this.categoryStatsDTO));

        Sort sort = Sort.by(CategorySortField.TOTAL_STARTED_QUIZZES.getFieldName()).descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(this.categoryStatsService.getAllCategoriesFiltered(anyLong(), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "TOTAL_STARTED_QUIZZES"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories-statistics"))
                .andExpect(model().attribute("categoriesStats", List.of(this.categoryStatsDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 1))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attribute("sortBy", CategorySortField.TOTAL_STARTED_QUIZZES));

        verify(this.categoryStatsService, times(1))
                .getAllCategoriesFiltered(anyLong(), eq(pageable));
    }
}