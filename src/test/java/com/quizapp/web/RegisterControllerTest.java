package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegisterController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = GlobalController.class)})
@Import(SecurityConfig.class)
public class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @WithAnonymousUser
    @Test
    void showRegisterPage_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegisterDTO"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showRegisterPage_ShouldReturnModelAndView_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/register"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showRegisterPage_ShouldReturnModelAndView_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/users/register"))
                .andExpect(status().isForbidden());
    }

    @WithAnonymousUser
    @Test
    void registerUser_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/users/register")
                    .with(csrf())
                    .param("username", "")
                    .param("email", "")
                    .param("password", "")
                    .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.userRegisterDTO"));

        verify(this.userService, never()).registerUser(any(UserRegisterDTO.class));
    }

    @WithAnonymousUser
    @Test
    void registerUser_ShouldRedirectWithError_WhenDataNotValid() throws Exception {
        when(this.userService.registerUser(any(UserRegisterDTO.class)))
                .thenReturn(new Result(false, "Паролите не съвпадат!"));

        this.mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .param("username", "newUser")
                        .param("email", "newuser@gmail.com")
                        .param("password", "Pass1234")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/register"))
                .andExpect(flash().attribute("error", "Паролите не съвпадат!"));

        verify(this.userService, times(1)).registerUser(any(UserRegisterDTO.class));
    }

    @WithAnonymousUser
    @Test
    void registerUser_ShouldRedirectWithSuccess_WhenDataIsValid() throws Exception {
        when(this.userService.registerUser(any(UserRegisterDTO.class)))
                .thenReturn(new Result(true, "Успешна регистрация!"));

        this.mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .param("username", "newUser")
                        .param("email", "newuser@gmail.com")
                        .param("password", "Password123")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"))
                .andExpect(flash().attribute("success", "Успешна регистрация!"));

        verify(this.userService, times(1)).registerUser(any(UserRegisterDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void registerUser_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .param("username", "newUser")
                        .param("email", "newuser@gmail.com")
                        .param("password", "Password123")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().isForbidden());

        verify(this.userService, never()).registerUser(any(UserRegisterDTO.class));
    }
}