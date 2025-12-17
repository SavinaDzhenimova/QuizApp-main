package com.quizapp.service;

import com.quizapp.model.dto.category.AddCategoryDTO;
import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.model.dto.category.UpdateCategoryDTO;
import com.quizapp.model.dto.question.AddQuestionDTO;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.question.QuestionPageDTO;
import com.quizapp.model.dto.question.UpdateQuestionDTO;
import com.quizapp.model.rest.CategoryApiDTO;
import com.quizapp.model.rest.QuestionApiDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceImplTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private QuestionServiceImpl questionService;

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

    private QuestionApiDTO api1;
    private QuestionApiDTO api2;
    private AddQuestionDTO addQuestionDTO;
    private UpdateQuestionDTO updateQuestionDTO;

    @BeforeEach
    void setUp() {
        this.api1 = QuestionApiDTO.builder()
                .id(1L)
                .categoryName("Maths")
                .questionText("Question 1")
                .correctAnswer("A")
                .options(new ArrayList<>())
                .build();

        this.api2 = QuestionApiDTO.builder()
                .id(2L)
                .categoryName("Music")
                .questionText("Question 2")
                .correctAnswer("B")
                .options(new ArrayList<>())
                .build();

        this.addQuestionDTO = AddQuestionDTO.builder()
                .categoryId(1L)
                .questionText("Add question")
                .correctAnswer("C")
                .options("A, B, C, D")
                .build();

        this.updateQuestionDTO = UpdateQuestionDTO.builder()
                .id(1L)
                .categoryName("Maths")
                .questionText("Updated question")
                .correctAnswer("B")
                .options("A, B, C, D")
                .build();
    }

    @Test
    void getAllQuestions_ShouldReturnMappedQuestionDTOs() {
        QuestionPageDTO<QuestionApiDTO> apiPage = new QuestionPageDTO<>();
        apiPage.setQuestions(List.of(this.api1, this.api2));
        apiPage.setTotalPages(1);
        apiPage.setTotalElements(2);
        apiPage.setCurrentPage(0);
        apiPage.setSize(10);

        Pageable pageable = PageRequest.of(0, 10);

        ParameterizedTypeReference<QuestionPageDTO<QuestionApiDTO>> typeRef =
                new ParameterizedTypeReference<>() {};

        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri(anyString())).thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(eq(typeRef))).thenReturn(apiPage);

        QuestionPageDTO<QuestionDTO> result =
                questionService.getAllQuestions("", null, pageable);

        Assertions.assertEquals(2, result.getQuestions().size());
        Assertions.assertEquals(1L, result.getQuestions().get(0).getId());
        Assertions.assertEquals(2L, result.getQuestions().get(1).getId());
        Assertions.assertEquals("Maths", result.getQuestions().get(0).getCategoryName());
        Assertions.assertEquals("Music", result.getQuestions().get(1).getCategoryName());
        Assertions.assertEquals("Question 1", result.getQuestions().get(0).getQuestionText());
        Assertions.assertEquals("Question 2", result.getQuestions().get(1).getQuestionText());
        Assertions.assertEquals("A", result.getQuestions().get(0).getCorrectAnswer());
        Assertions.assertEquals("B", result.getQuestions().get(1).getCorrectAnswer());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(10, result.getSize());
    }

    @Test
    void getAllQuestions_ShouldReturnCorrectDTO_WhenFilteredByQuestionTextAndCategoryId() {
        QuestionPageDTO<QuestionApiDTO> apiPage = new QuestionPageDTO<>();
        apiPage.setQuestions(List.of(this.api1));
        apiPage.setTotalPages(1);
        apiPage.setTotalElements(1);
        apiPage.setCurrentPage(0);
        apiPage.setSize(10);

        Pageable pageable = PageRequest.of(0, 10);

        ParameterizedTypeReference<QuestionPageDTO<QuestionApiDTO>> typeRef =
                new ParameterizedTypeReference<>() {};

        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri(anyString())).thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(eq(typeRef))).thenReturn(apiPage);

        QuestionPageDTO<QuestionDTO> result =
                questionService.getAllQuestions("Question", 1L, pageable);

        Assertions.assertEquals(1, result.getQuestions().size());
        Assertions.assertEquals(1L, result.getQuestions().get(0).getId());
        Assertions.assertEquals("Maths", result.getQuestions().get(0).getCategoryName());
        Assertions.assertEquals("Question 1", result.getQuestions().get(0).getQuestionText());
        Assertions.assertEquals("A", result.getQuestions().get(0).getCorrectAnswer());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(10, result.getSize());
    }

    @Test
    void getAllQuestions_ShouldReturnEmptyPage_WhenQuestionsNotFound() {
        QuestionPageDTO<QuestionApiDTO> apiPage = new QuestionPageDTO<>();
        apiPage.setQuestions(Collections.emptyList());
        apiPage.setTotalPages(1);
        apiPage.setTotalElements(0);
        apiPage.setCurrentPage(0);
        apiPage.setSize(10);

        Pageable pageable = PageRequest.of(0, 10);

        ParameterizedTypeReference<QuestionPageDTO<QuestionApiDTO>> typeRef =
                new ParameterizedTypeReference<>() {};

        when(this.restClient.get()).thenReturn(this.getSpec);
        when(this.getSpec.uri(anyString())).thenReturn(this.headersSpec);
        when(this.headersSpec.retrieve()).thenReturn(this.responseSpec);
        when(this.responseSpec.body(eq(typeRef))).thenReturn(apiPage);

        QuestionPageDTO<QuestionDTO> result =
                questionService.getAllQuestions("Question", 1L, pageable);

        Assertions.assertTrue(result.getQuestions().isEmpty());
        Assertions.assertEquals(0, result.getQuestions().size());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertEquals(0, result.getCurrentPage());
        Assertions.assertEquals(10, result.getSize());
    }
}