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

@WebMvcTest(controllers = HomeController.class)
@Import(SecurityConfig.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalController globalController;

    @WithAnonymousUser
    @Test
    void getIndexPage_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("subscribeDTO"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void getIndexPage_ShouldReturnModelAndView_WhenUser() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("subscribeDTO"));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void getIndexPage_ShouldReturnModelAndView_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("subscribeDTO"));
    }

    @WithAnonymousUser
    @Test
    void getAboutUsPage_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/about-us"))
                .andExpect(status().isOk())
                .andExpect(view().name("about-us"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void getAboutUsPage_ShouldReturnModelAndView_WhenUser() throws Exception {
        this.mockMvc.perform(get("/about-us"))
                .andExpect(status().isOk())
                .andExpect(view().name("about-us"));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void getAboutUsPage_ShouldReturnModelAndView_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/about-us"))
                .andExpect(status().isOk())
                .andExpect(view().name("about-us"));
    }
}