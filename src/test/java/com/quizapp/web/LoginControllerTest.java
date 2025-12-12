package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginController.class)
@Import(SecurityConfig.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalController globalController;

    @WithAnonymousUser
    @Test
    void login_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/users/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void login_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/login"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void login_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/users/login"))
                .andExpect(status().isForbidden());
    }

    @WithAnonymousUser
    @Test
    void loginError_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/users/login-error"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Невалидно потребителско име или парола!"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void loginError_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/login-error"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void loginError_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/users/login-error"))
                .andExpect(status().isForbidden());
    }
}