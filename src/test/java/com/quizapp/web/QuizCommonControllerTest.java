package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.exception.CategoryNotFoundException;
import com.quizapp.exception.GlobalExceptionHandler;
import com.quizapp.exception.NotEnoughQuestionsException;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.QuizCommonService;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuizCommonController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class QuizCommonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizCommonService quizCommonService;

    @MockitoBean
    private GlobalController globalController;

    private Quiz quiz;
    private UserDetailsDTO loggedUser;

    @BeforeEach
    void setUp() {
        this.loggedUser = UserDetailsDTO.builder()
                .id(1L)
                .username("logged")
                .email("logged@gmail.com")
                .password("Password123")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        this.quiz = Quiz.builder()
                .viewToken("token123")
                .build();
    }

    @WithAnonymousUser
    @Test
    void createQuiz_ShouldReturnErrorView_WhenNotEnoughQuestions() throws Exception {
        when(this.quizCommonService.createQuiz(5L, 5))
                .thenThrow(new NotEnoughQuestionsException("Броят на въпросите налични в тази категория не е достатъчен, за да започнете куиз."));

        this.mockMvc.perform(post("/quizzes/start")
                        .param("categoryId", "5")
                        .param("questionsCount", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/not-enough-questions"))
                .andExpect(model().attribute("message", "Броят на въпросите налични в тази категория не е достатъчен, за да започнете куиз."));
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

    @WithAnonymousUser
    @Test
    void showQuiz_ShouldReturnQuizAndAddSubmissionDTO_WhenNotPresent() throws Exception {
        when(this.quizCommonService.getQuizFromTemp("token123"))
                .thenReturn(this.quiz);

        this.mockMvc.perform(get("/quizzes/quiz/token123"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attributeExists("quizSubmissionDTO"))
                .andExpect(model().attributeExists("quiz"))
                .andExpect(model().attribute("quiz", this.quiz))
                .andExpect(model().attributeExists("isLogged"))
                .andExpect(model().attribute("isLogged", false));

        verify(this.quizCommonService, times(1)).getQuizFromTemp("token123");
    }

    @Test
    void showQuiz_ShouldNotOverrideQuizSubmissionDTO_WhenPresent() throws Exception {
        QuizSubmissionDTO quizSubmissionDTO = QuizSubmissionDTO.builder().viewToken("token123").build();

        when(this.quizCommonService.getQuizFromTemp("token123"))
                .thenReturn(this.quiz);

        this.mockMvc.perform(get("/quizzes/quiz/token123")
                    .with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attributeExists("quiz"))
                .andExpect(model().attribute("quiz", this.quiz))
                .andExpect(model().attributeExists("quizSubmissionDTO"))
                .andExpect(model().attribute("quizSubmissionDTO", quizSubmissionDTO))
                .andExpect(model().attributeExists("isLogged"))
                .andExpect(model().attribute("isLogged", true));

        verify(this.quizCommonService, times(1)).getQuizFromTemp("token123");
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showQuiz_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/quizzes/quiz/token123"))
                .andExpect(status().isForbidden());

        verify(this.quizCommonService, never()).getQuizFromTemp(anyString());
    }
}