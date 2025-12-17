package com.quizapp.service;

import com.quizapp.model.dto.category.AddCategoryDTO;
import com.quizapp.model.dto.category.UpdateCategoryDTO;
import com.quizapp.model.dto.question.AddQuestionDTO;
import com.quizapp.model.dto.question.UpdateQuestionDTO;
import com.quizapp.model.rest.CategoryApiDTO;
import com.quizapp.model.rest.QuestionApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;

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

    
}