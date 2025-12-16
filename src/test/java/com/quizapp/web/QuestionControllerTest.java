package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.question.QuestionPageDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.dto.user.UserStatsDTO;
import com.quizapp.service.interfaces.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuestionController.class)
@Import(SecurityConfig.class)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private GlobalController globalController;

    private UserDetailsDTO admin;
    private UserDetailsDTO user;
    private QuestionDTO questionDTO;

    @BeforeEach
    void setUp() {
        this.user = UserDetailsDTO.builder()
                .username("user")
                .email("user@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        this.admin = UserDetailsDTO.builder()
                .username("admin")
                .email("admin@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        this.questionDTO = QuestionDTO.builder()
                .id(1L)
                .categoryName("Maths")
                .questionText("Question text")
                .correctAnswer("A")
                .options(List.of("A", "B", "C", "D"))
                .build();
    }

    @Test
    void showQuestions_ShouldReturnQuestionsPage_WhenDataFound() throws Exception {
        Pageable pageable = PageRequest.of(0,10);
        QuestionPageDTO<QuestionDTO> page = new QuestionPageDTO<>(List.of(this.questionDTO));
        page.setTotalPages(1);
        page.setTotalElements(1);
        page.setSize(10);

        when(this.questionService.getAllQuestions("", 1L, pageable))
                .thenReturn(page);

        this.mockMvc.perform(get("/questions")
                        .with(user(this.admin))
                        .param("page", "0")
                        .param("size", "10")
                        .param("questionText", "")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("questions"))
                .andExpect(model().attributeExists("updateQuestionDTO"))
                .andExpect(model().attributeExists("questions"))
                .andExpect(model().attribute("questions", List.of(this.questionDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 10))
                .andExpect(model().attribute("questionText", ""))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attributeDoesNotExist("warning"));

        verify(this.questionService, times(1))
                .getAllQuestions("", 1L, pageable);
    }

    @Test
    void showQuestions_ShouldReturnEmptyPage_WhenDataNotFound() throws Exception {
        Pageable pageable = PageRequest.of(0,10);
        QuestionPageDTO<QuestionDTO> page = new QuestionPageDTO<>(Collections.emptyList());
        page.setTotalPages(1);
        page.setTotalElements(0);
        page.setSize(10);

        when(this.questionService.getAllQuestions("", 1L, pageable))
                .thenReturn(page);

        this.mockMvc.perform(get("/questions")
                        .with(user(this.admin))
                        .param("page", "0")
                        .param("size", "10")
                        .param("questionText", "")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("questions"))
                .andExpect(model().attributeExists("updateQuestionDTO"))
                .andExpect(model().attributeExists("questions"))
                .andExpect(model().attribute("questions", Collections.emptyList()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 0L))
                .andExpect(model().attribute("size", 10))
                .andExpect(model().attribute("questionText", ""))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Няма намерени въпроси за зададените критерии!"));

        verify(this.questionService, times(1))
                .getAllQuestions("", 1L, pageable);
    }

    @WithAnonymousUser
    @Test
    void showQuestions_ShouldRedirectToLoginPage_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/questions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("questionText", "")
                        .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.questionService, never())
                .getAllQuestions(anyString(), anyLong(), any(Pageable.class));
    }

    @Test
    void showQuestions_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/questions")
                        .with(user(this.user))
                        .param("page", "0")
                        .param("size", "10")
                        .param("questionText", "")
                        .param("categoryId", "1"))
                .andExpect(status().isForbidden());

        verify(this.questionService, never())
                .getAllQuestions(anyString(), anyLong(), any(Pageable.class));
    }
}