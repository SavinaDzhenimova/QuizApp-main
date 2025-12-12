package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.exception.CategoryNotFoundException;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.QuizCommonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuizCommonController.class)
@Import(SecurityConfig.class)
public class QuizCommonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizCommonService quizCommonService;

    @MockitoBean
    private GlobalController globalController;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        this.quiz = Quiz.builder()
                .viewToken("token123")
                .build();
    }

    @WithAnonymousUser
    @Test
    void createQuiz_ShouldRedirectToQuizPage_WhenQuizIsCreated() throws Exception {
        when(this.quizCommonService.createQuiz(anyLong(), anyInt()))
                .thenReturn(this.quiz);

        this.mockMvc.perform(post("/quizzes/start")
                    .with(csrf())
                    .param("categoryId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quizzes/quiz/token123?page=0"));

        verify(this.quizCommonService, times(1)).createQuiz(anyLong(), anyInt());
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void createQuiz_ShouldReturnError_WhenQuizNotCreated() throws Exception {
        when(this.quizCommonService.createQuiz(anyLong(), anyInt()))
                .thenThrow(new CategoryNotFoundException("Категорията не е намерена."));

        this.mockMvc.perform(post("/quizzes/start")
                    .with(csrf())
                    .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/object-not-found"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "Категорията не е намерена."));

        verify(this.quizCommonService, times(1)).createQuiz(anyLong(), anyInt());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void createQuiz_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(post("/quizzes/start")
                    .with(csrf())
                    .param("categoryId", "1"))
                .andExpect(status().isForbidden());
    }
}