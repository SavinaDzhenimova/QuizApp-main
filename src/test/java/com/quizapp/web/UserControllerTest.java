package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.user.UpdatePasswordDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.dto.user.UserStatsDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.UserQuizService;
import com.quizapp.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private UserDetailsDTO admin;
    private UserDTO userDTO;
    private UserDTO adminDTO;
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

        this.admin = UserDetailsDTO.builder()
                .username("admin")
                .email("admin@gmail.com")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        this.adminDTO = UserDTO.builder()
                .id(1L)
                .username("user")
                .email("user@gmail.com")
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
        when(this.userService.getUserInfo("admin"))
                .thenReturn(this.adminDTO);

        this.mockMvc.perform(get("/users/home")
                        .with(user(this.admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", this.adminDTO))
                .andExpect(model().attributeDoesNotExist("userStats"))
                .andExpect(model().attributeDoesNotExist("warning"));

        verify(this.userService, times(1)).getUserInfo("admin");
    }

    @Test
    void viewUserQuizzes_ShouldReturnEmptyPage_WhenQuizzesNotFound() throws Exception {
        when(this.userQuizService.getSolvedQuizzesByUsername(anyString(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        this.mockMvc.perform(get("/users/quizzes")
                        .with(user(this.loggedUser))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("quizzes"))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Все още нямате решени куизове."));

        verify(this.userQuizService, times(1))
                .getSolvedQuizzesByUsername(anyString(), anyInt(), anyInt());
    }

    @Test
    void viewUserQuizzes_ShouldReturnQuizzesPage_WhenQuizzesFound() throws Exception {
        QuizDTO quizDTO = QuizDTO.builder().id(1L).categoryId(5L).build();
        Page<QuizDTO> page = new PageImpl<>(List.of(quizDTO));

        when(this.userQuizService.getSolvedQuizzesByUsername(anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        this.mockMvc.perform(get("/users/quizzes")
                        .with(user(this.loggedUser))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("quizzes"))
                .andExpect(model().attributeDoesNotExist("warning"))
                .andExpect(model().attributeExists("quizzes"))
                .andExpect(model().attribute("quizzes", page.getContent()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(this.userQuizService, times(1))
                .getSolvedQuizzesByUsername(anyString(), anyInt(), anyInt());
    }

    @Test
    void viewUserQuizzes_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/users/quizzes")
                        .with(user(this.admin))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isForbidden());

        verify(this.userQuizService, never())
                .getSolvedQuizzesByUsername(anyString(), anyInt(), anyInt());
    }

    @WithAnonymousUser
    @Test
    void viewUserQuizzes_ShouldReturnError_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/users/quizzes")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.userQuizService, never())
                .getSolvedQuizzesByUsername(anyString(), anyInt(), anyInt());
    }

    @Test
    void showUpdatePasswordPage_ShouldAddAttribute_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/update-password")
                        .with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("update-password"))
                .andExpect(model().attributeExists("updatePasswordDTO"));
    }

    @Test
    void showUpdatePasswordPage_ShouldAddAttribute_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/users/update-password")
                        .with(user(this.admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("update-password"))
                .andExpect(model().attributeExists("updatePasswordDTO"));
    }

    @WithAnonymousUser
    @Test
    void showUpdatePasswordPage_ShouldAddAttribute_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/users/update-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));
    }

    @Test
    void showUpdatePasswordPage_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/users/update-password")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("oldPassword", "")
                        .param("newPassword", "")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("update-password"))
                .andExpect(model().attributeExists("updatePasswordDTO"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.updatePasswordDTO"));

        verify(this.userService, never())
                .updatePassword(anyString(), any(UpdatePasswordDTO.class));
    }

    @Test
    void showUpdatePasswordPage_ShouldRedirectWithError_WhenDataIsInvalid() throws Exception {
        when(this.userService.updatePassword(anyString(), any(UpdatePasswordDTO.class)))
                .thenReturn(new Result(false, "Паролите не съвпадат!"));

        this.mockMvc.perform(post("/users/update-password")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("oldPassword", "Pass1234")
                        .param("newPassword", "Password123")
                        .param("confirmPassword", "Password321"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/update-password"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Паролите не съвпадат!"));

        verify(this.userService, times(1))
                .updatePassword(anyString(), any(UpdatePasswordDTO.class));
    }

    @Test
    void showUpdatePasswordPage_ShouldRedirectWithSuccess_WhenDataIsValid() throws Exception {
        when(this.userService.updatePassword(anyString(), any(UpdatePasswordDTO.class)))
                .thenReturn(new Result(true, "Успешно променихте паролата си."));

        this.mockMvc.perform(post("/users/update-password")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("oldPassword", "Pass1234")
                        .param("newPassword", "Password123")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/update-password"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Успешно променихте паролата си."));

        verify(this.userService, times(1))
                .updatePassword(anyString(), any(UpdatePasswordDTO.class));
    }

    @Test
    void showUpdatePasswordPage_ShouldRedirectWithSuccess_WhenDataIsValidAndAdmin() throws Exception {
        when(this.userService.updatePassword(anyString(), any(UpdatePasswordDTO.class)))
                .thenReturn(new Result(true, "Успешно променихте паролата си."));

        this.mockMvc.perform(post("/users/update-password")
                        .with(csrf())
                        .with(user(this.admin))
                        .param("oldPassword", "Pass1234")
                        .param("newPassword", "Password123")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/update-password"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Успешно променихте паролата си."));

        verify(this.userService, times(1))
                .updatePassword(anyString(), any(UpdatePasswordDTO.class));
    }

    @WithAnonymousUser
    @Test
    void showUpdatePasswordPage_ShouldRedirectToLoginPage_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(post("/users/update-password")
                        .with(csrf())
                        .param("oldPassword", "Pass1234")
                        .param("newPassword", "Password123")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.userService, never())
                .updatePassword(anyString(), any(UpdatePasswordDTO.class));
    }
}