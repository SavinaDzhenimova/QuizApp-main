package com.quizapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.entity.Category;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<CategoryDTO> getAllCategories() {
        Category[] categories = restClient.get()
                .uri("/api/categories")
                .retrieve()
                .body(Category[].class);

        return Arrays.stream(categories)
                .map(this::categoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        try {
            Category category = restClient.get()
                    .uri("/api/categories/{id}", id)
                    .retrieve()
                    .body(Category.class);

            return this.categoryToDTO(category);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private CategoryDTO categoryToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    @Override
    public Object addCategory(AddCategoryDTO addCategoryDTO) {
        try {
            return restClient.post()
                    .uri("/api/categories")
                    .body(addCategoryDTO)
                    .retrieve()
                    .body(Category.class);
        } catch (HttpClientErrorException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();

            try {
                Map<String, String> errors = objectMapper.readValue(responseBody, Map.class);
                return Map.of("errors", errors);
            } catch (Exception ex) {
                return Map.of("errors", Map.of("general", "Нещо се обърка при обработката на грешката"));
            }
        }
    }

    @Override
    public boolean deleteCategoryById(Long id) {
        try {
            restClient.delete()
                    .uri("/api/categories/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public String getCategoryNameById(Long id) {
        try {
           return restClient.get()
                    .uri("/api/categories/{id}", id)
                    .retrieve()
                    .body(Category.class)
                    .getName();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}