package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.dto.user.UserStatsDTO;
import com.quizapp.service.interfaces.UserQuizService;
import com.quizapp.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserQuizService userQuizService;

    @MockitoBean
    private GlobalController globalController;

    private UserDetailsDTO loggedUser;
    private UserDTO userDTO;
    private UserStatsDTO userStatsDTO;

    @BeforeEach
    void setUp() {
        this.loggedUser = UserDetailsDTO.builder()
                .username("user")
                .email("user@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        this.userStatsDTO = UserStatsDTO.builder()
                .totalQuizzes(5)
                .maxScore(25)
                .score(18)
                .averageScore(72.00)
                .lastSolvedAt(LocalDateTime.now())
                .build();

        this.userDTO = UserDTO.builder()
                .id(1L)
                .username("user")
                .email("user@gmail.com")
                .userStats(this.userStatsDTO)
                .solvedQuizzes(new ArrayList<>())
                .build();
    }

    @Test
    void showHomePage_ShouldReturnUserInfoForHomePageAndAddWarningMessage_WhenDataIsValidAndSolvedQuizzesNotFound() throws Exception {
        when(this.userService.getUserInfo("user"))
                .thenReturn(this.userDTO);

        this.mockMvc.perform(get("/users/home")
                        .with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", this.userDTO))
                .andExpect(model().attributeExists("userStats"))
                .andExpect(model().attribute("userStats", this.userStatsDTO))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Все още нямате решени куизове."));

        verify(this.userService, times(1)).getUserInfo("user");
    }

    @Test
    void showHomePage_ShouldReturnUserInfoForHomePageWithoutWarningMessage_WhenDataIsValidAndSolvedQuizzesFound() throws Exception {
        QuizDTO quizDTO = QuizDTO.builder().id(1L).categoryId(5L).build();
        this.userDTO.getSolvedQuizzes().add(quizDTO);

        when(this.userService.getUserInfo("user"))
                .thenReturn(this.userDTO);

        this.mockMvc.perform(get("/users/home")
                        .with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", this.userDTO))
                .andExpect(model().attributeExists("userStats"))
                .andExpect(model().attribute("userStats", this.userStatsDTO))
                .andExpect(model().attributeDoesNotExist("warning"));

        verify(this.userService, times(1)).getUserInfo("user");
    }

    @WithAnonymousUser
    @Test
    void showHomePage_ShouldReturnError_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/users/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.userService, never()).getUserInfo("user");
    }

    @Test
    void showHomePage_ShouldReturnUserInfoForHomePageAndAddWarningMessage_WhenAdmin() throws Exception {
        UserDetailsDTO admin = UserDetailsDTO.builder()
                .username("admin")
                .email("admin@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        UserDTO adminDTO = UserDTO.builder()
                .id(1L)
                .username("user")
                .email("user@gmail.com")
                .build();

        when(this.userService.getUserInfo("admin"))
                .thenReturn(adminDTO);

        this.mockMvc.perform(get("/users/home")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", adminDTO))
                .andExpect(model().attributeDoesNotExist("userStats"))
                .andExpect(model().attributeDoesNotExist("warning"));

        verify(this.userService, times(1)).getUserInfo("admin");
    }
}