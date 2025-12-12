package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.ContactsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GlobalController.class, ContactsController.class})
@Import(SecurityConfig.class)
public class GlobalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ContactsService contactsService;

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void getCategoriesForSelectElement_ShouldReturnListCategoryDTOs_WhenCategoriesFound() throws Exception {
        CategoryDTO cat1 = CategoryDTO.builder().id(1L).name("Maths").description("Description").build();
        CategoryDTO cat2 = CategoryDTO.builder().id(2L).name("Music").description("Description").build();

        CategoryPageDTO<CategoryDTO> page = new CategoryPageDTO<>(List.of(cat1, cat2));

        when(this.categoryService.getAllCategories(anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        this.mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("problemTypes"))
                .andExpect(model().attributeExists("questionSortFields"))
                .andExpect(model().attributeExists("userSortFields"))
                .andExpect(model().attributeExists("categoriesSortFields"))
                .andExpect(model().attribute("categories", List.of(cat1, cat2)));

        verify(this.categoryService, times(1)).getAllCategories("", 0, 100);
    }

    @WithAnonymousUser
    @Test
    void getCategoriesForSelectElement_ShouldReturnEmptyPage_WhenCategoriesNotFound() throws Exception {
        CategoryPageDTO<CategoryDTO> page = new CategoryPageDTO<>(Collections.emptyList());

        when(this.categoryService.getAllCategories(anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        this.mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("problemTypes"))
                .andExpect(model().attributeExists("questionSortFields"))
                .andExpect(model().attributeExists("userSortFields"))
                .andExpect(model().attributeExists("categoriesSortFields"))
                .andExpect(model().attribute("categories", Collections.emptyList()));

        verify(this.categoryService, times(1)).getAllCategories("", 0, 100);
    }
}