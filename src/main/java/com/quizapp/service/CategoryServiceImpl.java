package com.quizapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.*;
import com.quizapp.model.rest.CategoryApiDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final RestClient restClient;

    @Override
    public CategoryPageDTO<CategoryDTO> getAllCategories(String categoryName, int page, int size) {

        CategoryPageDTO<CategoryApiDTO> categoryPageDTO = this.makeGetRequestAll(categoryName, page, size);

        List<CategoryDTO> categoryDTOs = categoryPageDTO.getCategories().stream()
                .map(this::categoryApiToDTO)
                .toList();

        CategoryPageDTO<CategoryDTO> categoryPage = new CategoryPageDTO<>();
        categoryPage.setCategories(categoryDTOs);
        categoryPage.setTotalPages(categoryPageDTO.getTotalPages());
        categoryPage.setTotalElements(categoryPageDTO.getTotalElements());
        categoryPage.setCurrentPage(categoryPageDTO.getCurrentPage());
        categoryPage.setSize(categoryPageDTO.getSize());

        return categoryPage;
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        try {
            CategoryApiDTO categoryApiDTO = this.makeGetRequestById(id);

            return this.categoryApiToDTO(categoryApiDTO);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private CategoryDTO categoryApiToDTO(CategoryApiDTO categoryApiDTO) {
        return CategoryDTO.builder()
                .id(categoryApiDTO.getId())
                .name(categoryApiDTO.getName())
                .description(categoryApiDTO.getDescription())
                .build();
    }

    @Override
    public Result addCategory(AddCategoryDTO addCategoryDTO) {
        try {
            this.makePostRequest(addCategoryDTO);
            return new Result(true, "Успешно добавихте категория " + addCategoryDTO.getName());

        } catch (HttpClientErrorException e) {

            String errorMessage = this.extractErrorMessage(e);
            return new Result(false, errorMessage);
        }
    }

    @Override
    public Result updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
        try {
            this.makePutRequest(id, updateCategoryDTO);
            return new Result(true, "Успешно редактирахте категория " + updateCategoryDTO.getName());

        } catch (HttpClientErrorException e) {
            String errorMessage = this.extractErrorMessage(e);
            return new Result(false, errorMessage);
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

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            ProblemDetailDTO problem = new ObjectMapper().readValue(body, ProblemDetailDTO.class);

            return problem.getDetail();
        } catch (Exception ex) {
            return "Грешка при извикване на REST API";
        }
    }

    private CategoryApiDTO makeGetRequestById(Long id) {
        return this.restClient.get()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .body(CategoryApiDTO.class);
    }

    private CategoryPageDTO<CategoryApiDTO> makeGetRequestAll(String categoryName, int page, int size) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/api/categories")
                .queryParam("page", page)
                .queryParam("size", size);

        if (categoryName != null && !categoryName.isBlank()) {
            uriBuilder.queryParam("categoryName", categoryName);
        }

        return this.restClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private ResponseEntity<Void> makePostRequest(AddCategoryDTO addCategoryDTO) {
        return this.restClient.post()
                .uri("/api/categories")
                .body(addCategoryDTO)
                .retrieve()
                .toBodilessEntity();
    }

    private ResponseEntity<Void> makePutRequest(Long id, UpdateCategoryDTO updateCategoryDTO) {
        return this.restClient.put()
                .uri("/api/categories/{id}", id)
                .body(updateCategoryDTO)
                .retrieve()
                .toBodilessEntity();
    }
}