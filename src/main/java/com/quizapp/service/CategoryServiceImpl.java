package com.quizapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.entity.Category;
import com.quizapp.model.entity.Result;
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
        Category[] categories = this.makeGetRequestAll();

        return Arrays.stream(categories)
                .map(this::categoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        try {
            Category category = this.makeGetRequest(id);

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
    public Result addCategory(AddCategoryDTO addCategoryDTO) {
        try {
            

            this.makePostRequest(addCategoryDTO);

            return new Result(true, "Успешно добавихте категория " + addCategoryDTO.getName());
        } catch (HttpClientErrorException.BadRequest e) {
            return new Result(false,
                    "Нещо се обърка! Категорията не беше запазена!");
        }
    }

    @Override
    public boolean deleteCategoryById(Long id) {
        try {
            this.makeDeleteRequest(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public String getCategoryNameById(Long id) {
        try {
           return this.makeGetRequest(id).getName();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private Category makeGetRequest(Long id) {
        return this.restClient.get()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .body(Category.class);
    }

    private Category[] makeGetRequestAll() {
        return this.restClient.get()
                .uri("/api/categories")
                .retrieve()
                .body(Category[].class);
    }

    private Category makePostRequest(AddCategoryDTO addCategoryDTO) {
        return this.restClient.post()
                .uri("/api/categories")
                .body(addCategoryDTO)
                .retrieve()
                .body(Category.class);
    }

    private void makeDeleteRequest(Long id) {
        this.restClient.delete()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}