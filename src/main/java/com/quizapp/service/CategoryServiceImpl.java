package com.quizapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.dto.UpdateCategoryDTO;
import com.quizapp.model.rest.CategoryApiDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.records.ApiError;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<CategoryDTO> getAllCategories() {
        CategoryApiDTO[] categoryApiDTOs = this.makeGetRequestAll();

        return Arrays.stream(categoryApiDTOs)
                .map(this::categoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        try {
            CategoryApiDTO categoryApiDTO = this.makeGetRequestById(id);

            return this.categoryToDTO(categoryApiDTO);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private CategoryDTO categoryToDTO(CategoryApiDTO category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    @Override
    public Result addCategory(AddCategoryDTO addCategoryDTO) throws JsonProcessingException {
        try {
            this.makePostRequest(addCategoryDTO);

            return new Result(true, "Успешно добавихте категория " + addCategoryDTO.getName());
        } catch (HttpClientErrorException e) {

            ApiError error = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ApiError.class
            );

            return new Result(false, error.message());
        }
    }

    @Override
    public Result updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
        try {
            CategoryApiDTO categoryApiDTO = this.makeGetRequestById(id);
            if (categoryApiDTO.getDescription().equals(updateCategoryDTO.getDescription())) {
                return new Result(false, "Няма промени за запазване");
            }

            this.makePutRequest(id, updateCategoryDTO);

            return new Result(true, "Успешно редактирахте категория " + updateCategoryDTO.getName());
        } catch (HttpClientErrorException e) {
            return new Result(false, "Грешка при редактиране! Категорията не можа да бъде променена.");
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
           return this.makeGetRequestById(id).getName();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private CategoryApiDTO makeGetRequestById(Long id) {
        return this.restClient.get()
                .uri("/api/categories/id/{id}", id)
                .retrieve()
                .body(CategoryApiDTO.class);
    }

    private CategoryApiDTO[] makeGetRequestAll() {
        return this.restClient.get()
                .uri("/api/categories")
                .retrieve()
                .body(CategoryApiDTO[].class);
    }

    private CategoryApiDTO makePostRequest(AddCategoryDTO addCategoryDTO) {
        return this.restClient.post()
                .uri("/api/categories")
                .body(addCategoryDTO)
                .retrieve()
                .body(CategoryApiDTO.class);
    }

    private CategoryApiDTO makePutRequest(Long id, UpdateCategoryDTO updateCategoryDTO) {
        return this.restClient.put()
                .uri("/api/categories/{id}", id)
                .body(updateCategoryDTO)
                .retrieve()
                .body(CategoryApiDTO.class);
    }

    private void makeDeleteRequest(Long id) {
        this.restClient.delete()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}