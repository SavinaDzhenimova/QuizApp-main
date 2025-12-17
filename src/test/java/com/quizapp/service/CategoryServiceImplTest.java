package com.quizapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.ProblemDetailDTO;
import com.quizapp.model.dto.category.AddCategoryDTO;
import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.model.dto.category.UpdateCategoryDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.rest.CategoryApiDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private RestClient.RequestHeadersUriSpec getSpec;
    @Mock
    private RestClient.RequestHeadersSpec headersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestBodyUriSpec postSpec;
    @Mock
    private RestClient.RequestBodyUriSpec putSpec;
    @Mock
    private RestClient.RequestBodySpec bodySpec;

    private CategoryApiDTO api1;
    private CategoryApiDTO api2;
    private AddCategoryDTO addCategoryDTO;
    private UpdateCategoryDTO updateCategoryDTO;

    @BeforeEach
    void setUp() {
        this.api1 = CategoryApiDTO.builder()
                .id(1L)
                .name("Maths")
                .description("Description")
                .questions(new ArrayList<>())
                .build();

        this.api2 = CategoryApiDTO.builder()
                .id(2L)
                .name("Music")
                .description("Description")
                .questions(new ArrayList<>())
                .build();

        this.addCategoryDTO = AddCategoryDTO.builder()
                .name("Maths")
                .description("Description")
                .build();

        this.updateCategoryDTO = UpdateCategoryDTO.builder()
                .name("Maths")
                .description("Updated")
                .build();
    }

    @Test
    void getAllCategories_ShouldReturnMappedDTOs() {
        CategoryPageDTO<CategoryApiDTO> apiPage = new CategoryPageDTO<>();
        apiPage.setCategories(List.of(this.api1, this.api2));
        apiPage.setTotalPages(1);
        apiPage.setTotalElements(2);
        apiPage.setCurrentPage(0);
        apiPage.setSize(10);

        ParameterizedTypeReference<CategoryPageDTO<CategoryApiDTO>> typeRef =
                new ParameterizedTypeReference<>() {};

        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri(anyString())).thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(eq(typeRef))).thenReturn(apiPage);

        CategoryPageDTO<CategoryDTO> result =
                categoryService.getAllCategories("", 0, 10);

        Assertions.assertEquals(2, result.getCategories().size());
        Assertions.assertEquals(1L, result.getCategories().get(0).getId());
        Assertions.assertEquals(2L, result.getCategories().get(1).getId());
        Assertions.assertEquals("Maths", result.getCategories().get(0).getName());
        Assertions.assertEquals("Music", result.getCategories().get(1).getName());
        Assertions.assertEquals("Description", result.getCategories().get(0).getDescription());
        Assertions.assertEquals("Description", result.getCategories().get(1).getDescription());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(10, result.getSize());
    }

    @Test
    void getAllCategories_ShouldReturnCorrectDTO_WhenFilteredByCategoryName() {
        CategoryPageDTO<CategoryApiDTO> apiPage = new CategoryPageDTO<>();
        apiPage.setCategories(List.of(this.api1));
        apiPage.setTotalPages(1);
        apiPage.setTotalElements(1);
        apiPage.setCurrentPage(0);
        apiPage.setSize(10);

        ParameterizedTypeReference<CategoryPageDTO<CategoryApiDTO>> typeRef =
                new ParameterizedTypeReference<>() {};

        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri(anyString())).thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(eq(typeRef))).thenReturn(apiPage);

        CategoryPageDTO<CategoryDTO> result =
                categoryService.getAllCategories("Maths", 0, 10);

        Assertions.assertEquals(1, result.getCategories().size());
        Assertions.assertEquals(1L, result.getCategories().get(0).getId());
        Assertions.assertEquals("Maths", result.getCategories().get(0).getName());
        Assertions.assertEquals("Description", result.getCategories().get(0).getDescription());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(10, result.getSize());
    }

    @Test
    void getAllCategories_ShouldReturnEmptyPage_WhenCategoriesNotFound() {
        CategoryPageDTO<CategoryApiDTO> apiPage = new CategoryPageDTO<>();
        apiPage.setCategories(Collections.emptyList());
        apiPage.setTotalPages(1);
        apiPage.setTotalElements(0);
        apiPage.setCurrentPage(0);
        apiPage.setSize(10);

        ParameterizedTypeReference<CategoryPageDTO<CategoryApiDTO>> typeRef =
                new ParameterizedTypeReference<>() {};

        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri(anyString())).thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(eq(typeRef))).thenReturn(apiPage);

        CategoryPageDTO<CategoryDTO> result =
                categoryService.getAllCategories("Maths", 0, 10);

        Assertions.assertTrue(result.getCategories().isEmpty());
        Assertions.assertEquals(0, result.getCategories().size());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(10, result.getSize());
    }

    @Test
    void getCategoryById_ShouldReturnCategoryDTO_WhenCategoryFound() {
        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri("/api/categories/{id}", 1L))
                .thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(CategoryApiDTO.class)).thenReturn(this.api1);

        CategoryDTO result = this.categoryService.getCategoryById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals("Maths", result.getName());
        Assertions.assertEquals("Description", result.getDescription());
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenCategoryNotFound() {
        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri("/api/categories/{id}", 1L))
                .thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(CategoryApiDTO.class))
                .thenThrow(HttpClientErrorException.NotFound
                        .create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        CategoryDTO result = this.categoryService.getCategoryById(1L);

        Assertions.assertNull(result);
    }

    @Test
    void getCategoryNameById_ShouldReturnCategoryName_WhenCategoryFound() {
        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri("/api/categories/{id}",1L))
                .thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(CategoryApiDTO.class)).thenReturn(this.api1);

        String result = this.categoryService.getCategoryNameById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Maths", result);
    }

    @Test
    void getCategoryNameById_ShouldThrowException_WhenCategoryNotFound() {
        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri("/api/categories/{id}", 1L))
                .thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(CategoryApiDTO.class))
                .thenThrow(HttpClientErrorException.NotFound
                        .create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        String result = this.categoryService.getCategoryNameById(1L);

        Assertions.assertNull(result);
    }

    @Test
    void addCategory_ShouldReturnSuccess_WhenDtoIsValid() {
        when(this.restClient.post()).thenReturn(this.postSpec);
        when(this.postSpec.uri("/api/categories")).thenReturn(this.bodySpec);
        when(this.bodySpec.body(this.addCategoryDTO)).thenReturn(this.bodySpec);
        when(this.bodySpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.toBodilessEntity())
                .thenReturn(ResponseEntity.ok().build());

        Result result = this.categoryService.addCategory(addCategoryDTO);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно добавихте категория Maths", result.getMessage());
    }

    @Test
    void addCategory_ShouldReturnError_WhenApiReturnsError() throws JsonProcessingException {
        ProblemDetailDTO problem = new ProblemDetailDTO();
        problem.setDetail("Категорията вече съществува");

        String jsonBody = new ObjectMapper().writeValueAsString(problem);

        HttpClientErrorException exception = HttpClientErrorException.BadRequest.create(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        jsonBody.getBytes(),
                        null);

        when(this.restClient.post()).thenReturn(this.postSpec);
        when(this.postSpec.uri("/api/categories")).thenReturn(this.bodySpec);
        when(this.bodySpec.body(this.addCategoryDTO)).thenReturn(this.bodySpec);
        when(this.bodySpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.toBodilessEntity()).thenThrow(exception);

        Result result = this.categoryService.addCategory(addCategoryDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Категорията вече съществува", result.getMessage());
    }

    @Test
    void addCategory_ShouldReturnError_WhenApiThrowsException() {
        String invalidJson = "this is not json";

        HttpClientErrorException exception =
                HttpClientErrorException.BadRequest.create(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        invalidJson.getBytes(),
                        null
                );

        when(this.restClient.post()).thenReturn(this.postSpec);
        when(this.postSpec.uri("/api/categories")).thenReturn(this.bodySpec);
        when(this.bodySpec.body(this.addCategoryDTO)).thenReturn(this.bodySpec);
        when(this.bodySpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.toBodilessEntity()).thenThrow(exception);

        Result result = categoryService.addCategory(addCategoryDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Грешка при извикване на REST API", result.getMessage());
    }

    @Test
    void updateCategory_ShouldReturnSuccessMessage_WhenDtoIsValid() {
        when(this.restClient.put()).thenReturn(this.putSpec);
        when(this.putSpec.uri("/api/categories/{id}", 1L)).thenReturn(this.bodySpec);
        when(this.bodySpec.body(this.updateCategoryDTO)).thenReturn(this.bodySpec);
        when(this.bodySpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.toBodilessEntity())
                .thenReturn(ResponseEntity.ok().build());

        Result result = this.categoryService.updateCategory(1L, this.updateCategoryDTO);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно редактирахте категория Maths", result.getMessage());
    }

    @Test
    void updateCategory_ShouldReturnErrorMessage_WhenApiReturnsError() throws JsonProcessingException {
        ProblemDetailDTO problem = new ProblemDetailDTO();
        problem.setDetail("Категорията не можа да бъде редактирана");

        String jsonBody = new ObjectMapper().writeValueAsString(problem);

        HttpClientErrorException exception = HttpClientErrorException.BadRequest.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                jsonBody.getBytes(),
                null);

        when(this.restClient.put()).thenReturn(this.putSpec);
        when(this.putSpec.uri("/api/categories/{id}",1L)).thenReturn(this.bodySpec);
        when(this.bodySpec.body(this.updateCategoryDTO)).thenReturn(this.bodySpec);
        when(this.bodySpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.toBodilessEntity()).thenThrow(exception);

        Result result = this.categoryService.updateCategory(1L, this.updateCategoryDTO);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Категорията не можа да бъде редактирана", result.getMessage());
    }
}