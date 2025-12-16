package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.service.interfaces.UserQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserQuizController.class)
@Import(SecurityConfig.class)
public class UserQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQuizService userQuizService;

    @MockitoBean
    private GlobalController globalController;

    private UserDetailsDTO loggedUser;

    @BeforeEach
    void setUp() {
        this.loggedUser = UserDetailsDTO.builder()
                .username("user")
                .email("user@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
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
    void submitQuiz_ShouldEvaluateQuiz_WhenDataIsValid() throws Exception {
        when(this.userQuizService
                .evaluateQuiz(any(QuizSubmissionDTO.class), anyString()))
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
}