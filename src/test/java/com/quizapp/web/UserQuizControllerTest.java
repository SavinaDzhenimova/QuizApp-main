package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.exception.CategoryStatisticsNotFound;
import com.quizapp.exception.GlobalExceptionHandler;
import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.service.interfaces.UserQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserQuizController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class UserQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQuizService userQuizService;

    @MockitoBean
    private GlobalController globalController;

    private UserDetailsDTO loggedUser;
    private QuizResultDTO quizResultDTO;
    private QuizDTO quizDTO;

    @BeforeEach
    void setUp() {
        this.loggedUser = UserDetailsDTO.builder()
                .username("user")
                .email("user@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        this.quizResultDTO = QuizResultDTO.builder()
                .id(1L)
                .totalQuestions(5)
                .correctAnswers(4)
                .scorePercent(80.00)
                .build();

        this.quizDTO = QuizDTO.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Maths")
                .build();
    }

    @Test
    void submitQuiz_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/users/quizzes/token123/submit")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("token", "token123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quizzes/quiz/token123"))
                .andExpect(flash().attributeExists("quizSubmissionDTO"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.quizSubmissionDTO"));

        verify(this.userQuizService, never())
                .evaluateQuiz(any(QuizSubmissionDTO.class), anyString());
    }

    @Test
    void submitQuiz_ShouldReturnErrorPage_WhenCategoryStatsNotFound() throws Exception {
        when(this.userQuizService.evaluateQuiz(any(QuizSubmissionDTO.class), anyString()))
                .thenThrow(new CategoryStatisticsNotFound("Не е намерена статистика за тази категория."));

        this.mockMvc.perform(post("/users/quizzes/token123/submit")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("token", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/object-not-found"))
                .andExpect(model().attribute("message", "Не е намерена статистика за тази категория."));

        verify(this.userQuizService, times(1))
                .evaluateQuiz(any(QuizSubmissionDTO.class), anyString());
    }

    @Test
    void submitQuiz_ShouldEvaluateQuiz_WhenDataIsValid() throws Exception {
        when(this.userQuizService.evaluateQuiz(any(QuizSubmissionDTO.class), anyString()))
                .thenReturn(1L);

        this.mockMvc.perform(post("/users/quizzes/token123/submit")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("token", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/quizzes/1/result"));

        verify(this.userQuizService, times(1))
                .evaluateQuiz(any(QuizSubmissionDTO.class), anyString());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void submitQuiz_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(post("/users/quizzes/token123/submit")
                        .with(csrf())
                        .param("token", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().isForbidden());

        verify(this.userQuizService, never())
                .evaluateQuiz(any(QuizSubmissionDTO.class), anyString());
    }

    @WithAnonymousUser
    @Test
    void submitQuiz_ShouldReturnError_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(post("/users/quizzes/token123/submit")
                        .with(csrf())
                        .param("token", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.userQuizService, never())
                .evaluateQuiz(any(QuizSubmissionDTO.class), anyString());
    }

    @Test
    void showSolvedQuizResult_ShouldReturnErrorPage_WhenQuizNotFound() throws Exception {
        when(this.userQuizService.getQuizResult(5L))
                .thenThrow(new QuizNotFoundException("Куизът не е намерен."));

        this.mockMvc.perform(get("/users/quizzes/5/result")
                        .with(user(this.loggedUser))
                        .param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/object-not-found"))
                .andExpect(model().attribute("message", "Куизът не е намерен."));

        verify(this.userQuizService, times(1))
                .getQuizResult(5L);
    }

    @Test
    void showSolvedQuizResult_ShouldReturnPageResult_WhenDataIsValid() throws Exception {
        when(this.userQuizService.getQuizResult(1L))
                .thenReturn(this.quizResultDTO);

        this.mockMvc.perform(get("/users/quizzes/1/result")
                        .with(user(this.loggedUser))
                        .param("id", "1L"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("result", this.quizResultDTO));

        verify(this.userQuizService, times(1)).getQuizResult(1L);
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showSolvedQuizResult_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/quizzes/1/result")
                        .param("id", "1L"))
                .andExpect(status().isForbidden());

        verify(this.userQuizService, never()).getQuizResult(anyLong());
    }

    @WithAnonymousUser
    @Test
    void showSolvedQuizResult_ShouldReturnError_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/users/quizzes/1/result")
                        .param("id", "1L"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.userQuizService, never()).getQuizResult(anyLong());
    }

    @Test
    void showSolvedQuizById_ShouldReturnErrorPage_WhenQuizNotFound() throws Exception {
        when(this.userQuizService.getSolvedQuizById(5L))
                .thenThrow(new QuizNotFoundException("Куизът не е намерен."));

        this.mockMvc.perform(get("/users/quizzes/5/review")
                        .with(user(this.loggedUser))
                        .param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/object-not-found"))
                .andExpect(model().attribute("message", "Куизът не е намерен."));

        verify(this.userQuizService, times(1))
                .getSolvedQuizById(5L);
    }

    @Test
    void showSolvedQuizById_ShouldReturnSolvedQuizPage_WhenDataIsValid() throws Exception {
        when(this.userQuizService.getSolvedQuizById(1L))
                .thenReturn(this.quizDTO);

        this.mockMvc.perform(get("/users/quizzes/1/review")
                        .with(user(this.loggedUser))
                        .param("id", "1L"))
                .andExpect(status().isOk())
                .andExpect(view().name("solved-quiz"))
                .andExpect(model().attributeExists("solvedQuiz"))
                .andExpect(model().attribute("solvedQuiz", this.quizDTO));

        verify(this.userQuizService, times(1)).getSolvedQuizById(anyLong());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showSolvedQuizById_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/quizzes/1/review")
                        .param("id", "1L"))
                .andExpect(status().isForbidden());

        verify(this.userQuizService, never()).getSolvedQuizById(anyLong());
    }

    @WithAnonymousUser
    @Test
    void showSolvedQuizById_ShouldReturnError_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/users/quizzes/1/review")
                        .param("id", "1L"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.userQuizService, never()).getSolvedQuizById(anyLong());
    }
}