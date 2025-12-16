package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.service.interfaces.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = CategoryController.class)
@Import(SecurityConfig.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private GlobalController globalController;

    private UserDetailsDTO user;
    private UserDetailsDTO admin;
    private CategoryDTO categoryDTO;

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

        this.categoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Maths")
                .description("Description")
                .build();
    }

    @Test
    void showCategoriesPage_ShouldReturnCategoriesPage_WhenCategoriesFound() throws Exception {
        CategoryPageDTO<CategoryDTO> page = new CategoryPageDTO<>(List.of(this.categoryDTO));
        page.setTotalPages(1);
        page.setTotalElements(1);
        page.setSize(10);

        when(this.categoryService.getAllCategories("", 0, 10))
                .thenReturn(page);

        this.mockMvc.perform(get("/categories")
                        .with(user(this.admin))
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("updateCategoryDTO"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("categories", List.of(this.categoryDTO)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 10))
                .andExpect(model().attribute("categoryName", ""))
                .andExpect(model().attributeDoesNotExist("warning"));

        verify(this.categoryService, times(1))
                .getAllCategories("", 0, 10);
    }

    @Test
    void showCategoriesPage_ShouldReturnEmptyPage_WhenCategoriesNotFound() throws Exception {
        CategoryPageDTO<CategoryDTO> page = new CategoryPageDTO<>(Collections.emptyList());
        page.setTotalPages(1);
        page.setTotalElements(0L);
        page.setSize(10);

        when(this.categoryService.getAllCategories("", 0, 10))
                .thenReturn(page);

        this.mockMvc.perform(get("/categories")
                        .with(user(this.admin))
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("updateCategoryDTO"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("categories", Collections.emptyList()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 0L))
                .andExpect(model().attribute("size", 10))
                .andExpect(model().attribute("categoryName", ""))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Няма намерени категории за зададените критерии!"));

        verify(this.categoryService, times(1))
                .getAllCategories("", 0, 10);
    }

    @Test
    void showCategoriesPage_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(get("/categories")
                        .with(user(this.user))
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryName", ""))
                .andExpect(status().isForbidden());

        verify(this.categoryService, never())
                .getAllCategories("", 0, 10);
    }

    @WithAnonymousUser
    @Test
    void showCategoriesPage_ShouldReturnError_WhenAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryName", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.categoryService, never())
                .getAllCategories("", 0, 10);
    }
}