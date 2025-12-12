package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.SubscribeDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.SubscribeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubscribeController.class)
@Import(SecurityConfig.class)
public class SubscribeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscribeService subscribeService;

    @MockitoBean
    private GlobalController globalController;

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void subscribe_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/subscribe")
                        .with(csrf())
                        .param("email", "invalid email"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/#subscribe"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.subscribeDTO"));

        verify(subscribeService, never()).subscribe(any(SubscribeDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void subscribe_ShouldReturnError_WhenUserIsAdmin() throws Exception {
        this.mockMvc.perform(post("/subscribe")
                        .with(csrf())
                        .param("email", "john@gmail.com"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void subscribe_ShouldRedirectWithError_WhenDataIsInvalid() throws Exception {
        when(this.subscribeService.subscribe(any(SubscribeDTO.class)))
                .thenReturn(new Result(false,"Вече сте абониран за новостите в QuizApp."));

        this.mockMvc.perform(post("/subscribe")
                    .with(csrf())
                    .param("email", "john@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/#subscribe"))
                .andExpect(flash().attribute("error", "Вече сте абониран за новостите в QuizApp."));

        verify(this.subscribeService, times(1)).subscribe(any(SubscribeDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void subscribe_ShouldRedirectWithSuccess_WhenDataIsValid() throws Exception {
        when(this.subscribeService.subscribe(any(SubscribeDTO.class)))
                .thenReturn(new Result(true,"Успешно се абонирахте."));

        this.mockMvc.perform(post("/subscribe")
                        .with(csrf())
                        .param("email", "john@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/#subscribe"))
                .andExpect(flash().attribute("success", "Успешно се абонирахте."));

        verify(this.subscribeService, times(1)).subscribe(any(SubscribeDTO.class));
    }
}