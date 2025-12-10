package com.quizapp.service;

import com.quizapp.exception.CategoryNotFoundException;
import com.quizapp.exception.NoQuestionsFoundException;
import com.quizapp.exception.NotEnoughQuestionsException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import com.quizapp.service.utils.TempQuizStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizCommonServiceTest {

    @Mock
    private TempQuizStorage mockTempQuizStorage;
    @Mock
    private QuestionService mockQuestionService;
    @Mock
    private CategoryService mockCategoryService;
    @Mock
    private CategoryStatisticsService mockCategoryStatsService;
    @Mock
    private QuestionStatisticsService mockQuestionStatsService;
    @InjectMocks
    private QuizCommonService mockQuizCommonService;

    private QuestionApiDTO question1;
    private QuestionApiDTO question2;
    private Quiz mockQuiz;

    @BeforeEach
    void setUp() {
        this.mockQuizCommonService = new QuizCommonService(
                this.mockTempQuizStorage,
                this.mockQuestionService,
                this.mockCategoryService,
                this.mockCategoryStatsService,
                this.mockQuestionStatsService);

        this.question1 = QuestionApiDTO.builder()
                .id(1L)
                .categoryId(5L)
                .categoryName("Math")
                .questionText("Question1")
                .correctAnswer("A")
                .options(List.of("A", "B", "C", "D"))
                .build();

        this.question2 = QuestionApiDTO.builder()
                .id(2L)
                .categoryId(5L)
                .categoryName("Math")
                .questionText("Question2")
                .correctAnswer("B")
                .options(List.of("A", "B", "C", "D"))
                .build();

        QuestionDTO questionDTO1 = QuestionDTO.builder()
                .id(1L)
                .categoryName("Math")
                .questionText("Question1")
                .correctAnswer("A")
                .options(List.of("A", "B", "C", "D"))
                .build();

        QuestionDTO questionDTO2 = QuestionDTO.builder()
                .id(2L)
                .categoryName("Math")
                .questionText("Question2")
                .correctAnswer("B")
                .options(List.of("A", "B", "C", "D"))
                .build();

        this.mockQuiz = Quiz.builder()
                .id(1L)
                .categoryId(5L)
                .categoryName("Math")
                .viewToken("token123")
                .questions(List.of(questionDTO1, questionDTO2))
                .build();
    }

    @Test
    void getQuizFromTemp_ShouldReturnQuiz_WhenDataIsValid() {
        when(this.mockTempQuizStorage.get("token123")).thenReturn(this.mockQuiz);

        Quiz result = this.mockQuizCommonService.getQuizFromTemp("token123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.mockQuiz.getViewToken(), result.getViewToken());
        verify(this.mockTempQuizStorage).get("token123");
    }

    @Test
    void createQuiz_ShouldReturnError_WhenCategoryNotFound() {
        when(this.mockCategoryService.getCategoryNameById(5L)).thenReturn("");

        CategoryNotFoundException exception = Assertions.assertThrows(CategoryNotFoundException.class,
                () -> this.mockQuizCommonService.createQuiz(5L, 2));

        Assertions.assertEquals("Категорията не е намерена.", exception.getMessage());
    }

    @Test
    void createQuiz_ShouldReturnError_WhenNoQuestionsFound() {
        when(this.mockCategoryService.getCategoryNameById(5L))
                .thenReturn("Math");

        when(this.mockQuestionService.makeGetRequestByCategoryId(5L))
                .thenReturn(new QuestionApiDTO[]{});

        NoQuestionsFoundException exception = Assertions.assertThrows(NoQuestionsFoundException.class,
                () -> this.mockQuizCommonService.createQuiz(5L, 5));

        Assertions.assertEquals("Няма налични въпроси в тази категория.", exception.getMessage());
    }

    @Test
    void createQuiz_ShouldReturnError_WhenNotEnoughQuestions() {
        when(this.mockCategoryService.getCategoryNameById(5L))
                .thenReturn("Math");

        when(this.mockQuestionService.makeGetRequestByCategoryId(5L))
                .thenReturn(new QuestionApiDTO[]{this.question1, this.question2});

        NotEnoughQuestionsException exception = Assertions.assertThrows(NotEnoughQuestionsException.class,
                () -> this.mockQuizCommonService.createQuiz(5L, 5));

        Assertions.assertEquals("Броят на въпросите налични в тази категория не е достатъчен, за да започнете куиз.", exception.getMessage());
    }

    @Test
    void createQuiz_ShouldCreateQuiz_WhenDataIsValid() {
        when(this.mockCategoryService.getCategoryNameById(5L))
                .thenReturn("Math");
        when(this.mockQuestionService.makeGetRequestByCategoryId(5L))
                .thenReturn(new QuestionApiDTO[]{this.question1, this.question2});
        when(this.mockQuestionService.mapQuestionApiToDTO(any(QuestionApiDTO.class)))
                .thenAnswer(inv -> {
                    QuestionApiDTO api = inv.getArgument(0);
                    return QuestionDTO.builder()
                            .id(api.getId())
                            .categoryName(api.getCategoryName())
                            .questionText(api.getQuestionText())
                            .correctAnswer(api.getCorrectAnswer())
                            .options(new ArrayList<>(api.getOptions()))
                            .build();
                });

        Quiz createdQuiz = this.mockQuizCommonService.createQuiz(5L, 2);

        Assertions.assertNotNull(createdQuiz);
        verify(this.mockQuestionStatsService, times(2))
                .increaseUsedQuestion(anyLong(), anyString(), eq(5L));
        verify(this.mockCategoryStatsService).increaseStartedQuizzes(5L);
        verify(this.mockTempQuizStorage).put(eq(createdQuiz.getViewToken()), eq(createdQuiz));

        Assertions.assertNull(createdQuiz.getId());
        Assertions.assertEquals(this.mockQuiz.getCategoryId(), createdQuiz.getCategoryId());
        Assertions.assertEquals(this.mockQuiz.getCategoryName(), createdQuiz.getCategoryName());
        Assertions.assertNotNull(createdQuiz.getViewToken());
        Assertions.assertNotNull(createdQuiz.getQuestions());
        Assertions.assertNotNull(createdQuiz.getExpireAt());
        Assertions.assertFalse(createdQuiz.getQuestions().isEmpty());
        Assertions.assertEquals(this.mockQuiz.getQuestions().size(), createdQuiz.getQuestions().size());
    }
}