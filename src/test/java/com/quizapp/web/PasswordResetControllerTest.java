package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.PasswordResetService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PasswordResetController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = GlobalController.class)})
@Import(SecurityConfig.class)
public class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @WithAnonymousUser
    @Test
    void showForgotPasswordPage_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/users/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showForgotPasswordPage_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/users/forgot-password"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void showForgotPasswordPage_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/users/forgot-password"))
                .andExpect(status().isForbidden());
    }

    @WithAnonymousUser
    @Test
    void sendForgotPasswordEmail_ShouldReturnError_WhenDataNotValid() throws Exception {
        when(this.passwordResetService.sendEmailForForgottenPassword(anyString()))
                .thenReturn(new Result(false, "Не открихме потребител с посочения имейл!"));

        this.mockMvc.perform(post("/users/forgot-password")
                    .with(csrf())
                    .param("email", "missing@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/forgot-password"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Не открихме потребител с посочения имейл!"));

        verify(this.passwordResetService, times(1)).sendEmailForForgottenPassword(anyString());
    }

    @WithAnonymousUser
    @Test
    void sendForgotPasswordEmail_ShouldReturnSuccess_WhenDataIsValid() throws Exception {
        when(this.passwordResetService.sendEmailForForgottenPassword(anyString()))
                .thenReturn(new Result(true, "Моля проверете пощата си за имейл с линк за смяна на паролата!"));

        this.mockMvc.perform(post("/users/forgot-password")
                        .with(csrf())
                        .param("email", "existing@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Моля проверете пощата си за имейл с линк за смяна на паролата!"));

        verify(this.passwordResetService, times(1)).sendEmailForForgottenPassword(anyString());
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void sendForgotPasswordEmail_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(post("/users/forgot-password")
                        .with(csrf())
                        .param("email", "existing@gmail.com"))
                .andExpect(status().isForbidden());

        verify(this.passwordResetService, never()).sendEmailForForgottenPassword(anyString());
    }
}