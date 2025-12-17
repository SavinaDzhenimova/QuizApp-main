package com.quizapp.service;

import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.model.rest.CategoryApiDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
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
    private RestClient.RequestBodySpec bodySpec;

    @Test
    void getAllCategories_ShouldReturnMappedDTOs() {

        CategoryApiDTO api1 = CategoryApiDTO.builder()
                .id(1L)
                .name("Maths")
                .description("Description")
                .questions(new ArrayList<>())
                .build();
        CategoryApiDTO api2 = CategoryApiDTO.builder()
                .id(2L)
                .name("Music")
                .description("Description")
                .questions(new ArrayList<>())
                .build();

        CategoryPageDTO<CategoryApiDTO> apiPage = new CategoryPageDTO<>();
        apiPage.setCategories(List.of(api1, api2));
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
}