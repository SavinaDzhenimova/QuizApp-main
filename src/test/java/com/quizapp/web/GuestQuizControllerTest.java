package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.service.interfaces.GuestQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {GuestQuizController.class})
@Import(SecurityConfig.class)
public class GuestQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestQuizService guestQuizService;

    @MockitoBean
    private GlobalController globalController;

    private QuizSubmissionDTO quizSubmissionDTO;

    @BeforeEach
    void setUp() {
        this.quizSubmissionDTO = QuizSubmissionDTO.builder()
                .viewToken("token123")
                .answers(Map.of(1L, "A", 2L, "B"))
                .build();
    }

    @WithAnonymousUser
    @Test
    void submitQuiz_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/guest/quizzes/token123/submit")
                        .with(csrf())
                        .param("viewToken", "token123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quizzes/quiz/token123"))
                .andExpect(flash().attributeExists("quizSubmissionDTO"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.quizSubmissionDTO"));

        verify(this.guestQuizService, never()).evaluateQuiz(any());
    }

    @WithAnonymousUser
    @Test
    void submitQuiz_ShouldEvaluateQuiz_WhenDataIsValid() throws Exception {
        this.mockMvc.perform(post("/guest/quizzes/token123/submit")
                        .with(csrf())
                        .param("viewToken", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/guest/quizzes/token123/result"));

        verify(this.guestQuizService, times(1)).evaluateQuiz(any(QuizSubmissionDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void submitQuiz_ShouldEvaluateQuiz_WhenDataIsValidAndUser() throws Exception {
        this.mockMvc.perform(post("/guest/quizzes/token123/submit")
                        .with(csrf())
                        .param("viewToken", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().isForbidden());

        verify(this.guestQuizService, never()).evaluateQuiz(any(QuizSubmissionDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void submitQuiz_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(post("/guest/quizzes/token123/submit")
                        .with(csrf())
                        .param("viewToken", "token123")
                        .param("answers[1]", "A")
                        .param("answers[2]", "B"))
                .andExpect(status().isForbidden());

        verify(this.guestQuizService, never()).evaluateQuiz(any(QuizSubmissionDTO.class));
    }
}