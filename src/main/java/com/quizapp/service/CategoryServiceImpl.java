package com.quizapp.service;

import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.dto.CategoryPageDTO;
import com.quizapp.model.dto.UpdateCategoryDTO;
import com.quizapp.model.enums.ApiResponse;
import com.quizapp.model.rest.CategoryApiDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final RestClient restClient;

    @Override
    public CategoryPageDTO<CategoryDTO> getAllCategories(Pageable pageable) {

        CategoryPageDTO<CategoryApiDTO> categoryPageDTO = this.makeGetRequestAll(pageable.getPageNumber(),
                pageable.getPageSize());

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
    public List<CategoryDTO> getAllCategories() {
        return Arrays.stream(this.makeGetRequestAll())
                .map(this::categoryApiToDTO)
                .toList();
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
            ResponseEntity<?> response = this.makePostRequest(addCategoryDTO);
            ApiResponse apiResponse = ApiResponse.fromStatus(response.getStatusCode());

            return switch (apiResponse) {
                case BAD_REQUEST -> new Result(false, "Невалидни входни данни!");
                case CREATED -> new Result(true, "Успешно добавихте категория " + addCategoryDTO.getName());
                default -> new Result(false, "Сървърна грешка при създаване на категория!");
            };

        } catch (HttpClientErrorException e) {

            return new Result(false, "Нещо се обърка! Категорията не беше записана.");
        }
    }

    @Override
    public Result updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
        try {
            CategoryApiDTO categoryApiDTO = this.makeGetRequestById(id);

            if (categoryApiDTO == null) {
                return new Result(false, "Категорията не е намерена!");
            }

            if (categoryApiDTO.getDescription().equals(updateCategoryDTO.getDescription())) {
                return new Result(false, "Няма промени за запазване");
            }

            ResponseEntity<Void> response = this.makePutRequest(id, updateCategoryDTO);
            ApiResponse apiResponse = ApiResponse.fromStatus(response.getStatusCode());

            if (apiResponse.equals(ApiResponse.SUCCESS)) {
                return new Result(true, "Успешно редактирахте категория " + updateCategoryDTO.getName());
            }

            return new Result(false, "Сървърна грешка при редактиране!");
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
                .uri("/api/categories/{id}", id)
                .retrieve()
                .body(CategoryApiDTO.class);
    }

    private CategoryPageDTO<CategoryApiDTO> makeGetRequestAll(int page, int size) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/api/categories")
                .queryParam("page", page)
                .queryParam("size", size);

        return this.restClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private CategoryApiDTO[] makeGetRequestAll() {
        return this.restClient.get()
                .uri("/api/categories")
                .retrieve()
                .body(CategoryApiDTO[].class);
    }

    private ResponseEntity<?> makePostRequest(AddCategoryDTO addCategoryDTO) {
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

    private void makeDeleteRequest(Long id) {
        this.restClient.delete()
                .uri("/api/categories/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}