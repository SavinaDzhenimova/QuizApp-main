package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.dto.question.QuestionStatsDTO;
import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.dto.user.UserStatsDTO;
import com.quizapp.model.enums.CategorySortField;
import com.quizapp.model.enums.QuestionSortField;
import com.quizapp.model.enums.UserSortField;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import com.quizapp.service.interfaces.UserStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
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
    private QuestionStatsDTO questionStatsDTO;
    private UserStatisticsDTO userStatisticsDTO;

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

        this.questionStatsDTO = QuestionStatsDTO.builder()
                .categoryId(1L)
                .questionId(1L)
                .categoryName("Maths")
                .attempts(10)
                .correctAnswers(9)
                .accuracy(90.00)
                .difficulty(10.00)
                .build();

        this.userStatisticsDTO = UserStatisticsDTO.builder()
                .totalQuizzes(10)
                .maxScore(50)
                .totalCorrectAnswers(45)
                .averageScore(90.00)
                .lastSolvedAt(LocalDateTime.now())
                .build();
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showCategoriesStats_ShouldReturnPageCategoryStatsFiltered_WhenDataFound() throws Exception {
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

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showCategoriesStats_ShouldReturnPageCategoryStatsNotFiltered_WhenNoSortBy() throws Exception {
        Page<CategoryStatsDTO> page = new PageImpl<>(List.of(this.categoryStatsDTO));

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        when(this.categoryStatsService.getAllCategoriesFiltered(anyLong(), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories-statistics"))
                .andExpect(model().attribute("categoriesStats", List.of(this.categoryStatsDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 1))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attribute("sortBy", nullValue()));

        verify(this.categoryStatsService, times(1))
                .getAllCategoriesFiltered(anyLong(), eq(pageable));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showCategoriesStats_ShouldReturnEmptyPage_WhenCategoriesNotFound() throws Exception {
        Page<CategoryStatsDTO> page = new PageImpl<>(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        when(this.categoryStatsService.getAllCategoriesFiltered(anyLong(), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories-statistics"))
                .andExpect(model().attribute("categoriesStats", Collections.emptyList()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 0L))
                .andExpect(model().attribute("size", 0))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attribute("sortBy", nullValue()))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Няма намерени статистики за категории."));

        verify(this.categoryStatsService, times(1))
                .getAllCategoriesFiltered(anyLong(), eq(pageable));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showCategoriesStats_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/statistics/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "TOTAL_STARTED_QUIZZES"))
                .andExpect(status().isForbidden());

        verify(this.categoryStatsService, never()).getAllCategoriesFiltered(anyLong(), any(Pageable.class));
    }

    @WithAnonymousUser
    @Test
    void showCategoriesStats_ShouldRedirectToLoginError_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/statistics/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "TOTAL_STARTED_QUIZZES"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.categoryStatsService, never()).getAllCategoriesFiltered(anyLong(), any(Pageable.class));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showQuestionsStats_ShouldReturnPageQuestionsStatsFiltered_WhenDataFound() throws Exception {
        Page<QuestionStatsDTO> page = new PageImpl<>(List.of(this.questionStatsDTO));

        Sort sort = Sort.by(QuestionSortField.COMPLETION_RATE.getFieldName()).descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(this.questionStatsService.getFilteredQuestionStatistics(anyLong(), anyString(), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/questions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "COMPLETION_RATE")
                        .param("questionText", "Question"))
                .andExpect(status().isOk())
                .andExpect(view().name("questions-statistics"))
                .andExpect(model().attribute("questionStats", List.of(this.questionStatsDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 1))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attribute("sortBy", QuestionSortField.COMPLETION_RATE))
                .andExpect(model().attribute("questionText", "Question"));

        verify(this.questionStatsService, times(1))
                .getFilteredQuestionStatistics(anyLong(), anyString(), eq(pageable));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showQuestionsStats_ShouldReturnPageQuestionStatsNotFiltered_WhenNoSortBy() throws Exception {
        Page<QuestionStatsDTO> page = new PageImpl<>(List.of(this.questionStatsDTO));

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        when(this.questionStatsService.getFilteredQuestionStatistics(anyLong(), anyString(), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/questions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "")
                        .param("questionText", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("questions-statistics"))
                .andExpect(model().attribute("questionStats", List.of(this.questionStatsDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 1))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attribute("sortBy", nullValue()))
                .andExpect(model().attribute("questionText", ""));

        verify(this.questionStatsService, times(1))
                .getFilteredQuestionStatistics(anyLong(), anyString(), eq(pageable));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showQuestionsStats_ShouldReturnEmptyPage_WhenQuestionsNotFound() throws Exception {
        Page<QuestionStatsDTO> page = new PageImpl<>(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        when(this.questionStatsService.getFilteredQuestionStatistics(anyLong(), anyString(), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/questions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "")
                        .param("questionText", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("questions-statistics"))
                .andExpect(model().attribute("questionStats", Collections.emptyList()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 0L))
                .andExpect(model().attribute("size", 0))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attribute("sortBy", nullValue()))
                .andExpect(model().attribute("questionText", ""))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Няма намерени статистики за въпроси."));

        verify(this.questionStatsService, times(1))
                .getFilteredQuestionStatistics(anyLong(), anyString(), eq(pageable));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showQuestionsStats_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/statistics/questions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "COMPLETION_RATE")
                        .param("questionText", ""))
                .andExpect(status().isForbidden());

        verify(this.questionStatsService, never())
                .getFilteredQuestionStatistics(anyLong(), anyString(), any(Pageable.class));
    }

    @WithAnonymousUser
    @Test
    void showQuestionsStats_ShouldRedirectToLoginError_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/statistics/questions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("sortBy", "COMPLETION_RATE")
                        .param("questionText", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.questionStatsService, never())
                .getFilteredQuestionStatistics(anyLong(), anyString(), any(Pageable.class));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showUserStats_ShouldReturnPageUserStatsFiltered_WhenDataFound() throws Exception {
        Page<UserStatisticsDTO> page = new PageImpl<>(List.of(this.userStatisticsDTO));

        Sort sort = Sort.by(UserSortField.TOTAL_CORRECT_ANSWERS.getFieldName()).descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(this.userStatsService.getUserStatisticsFiltered(anyString(), any(UserSortField.class), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "username")
                        .param("sortBy", "TOTAL_CORRECT_ANSWERS"))
                .andExpect(status().isOk())
                .andExpect(view().name("users-statistics"))
                .andExpect(model().attribute("userStats", List.of(this.userStatisticsDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 1))
                .andExpect(model().attribute("username", "username"))
                .andExpect(model().attribute("sortBy", UserSortField.TOTAL_CORRECT_ANSWERS));

        verify(this.userStatsService, times(1))
                .getUserStatisticsFiltered(anyString(), any(UserSortField.class), eq(pageable));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showUserStats_ShouldReturnPageUserStatsNotFiltered_WhenNoSortBy() throws Exception {
        Page<UserStatisticsDTO> page = new PageImpl<>(List.of(this.userStatisticsDTO));

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
        when(this.userStatsService.getUserStatisticsFiltered(anyString(), eq(null), eq(pageable)))
                .thenReturn(page);

        this.mockMvc.perform(get("/statistics/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "")
                        .param("sortBy", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("users-statistics"))
                .andExpect(model().attribute("userStats", List.of(this.userStatisticsDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 1))
                .andExpect(model().attribute("username", ""))
                .andExpect(model().attribute("sortBy", nullValue()));

        verify(this.userStatsService, times(1))
                .getUserStatisticsFiltered(anyString(), eq(null), eq(pageable));
    }
}