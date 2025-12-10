package com.quizapp.service;

import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import com.quizapp.service.utils.GuestQuizStorage;
import com.quizapp.service.utils.TempQuizStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GuestQuizServiceImplTest {

    @Mock
    private TempQuizStorage mockTempQuizStorage;
    @Mock
    private GuestQuizStorage mockGuestQuizStorage;
    @Mock
    private CategoryStatisticsService mockCategoryStatsService;
    @Mock
    private QuestionStatisticsService mockQuestionStatsService;
    @InjectMocks
    private GuestQuizServiceImpl mockGuestQuizService;

    private Quiz mockQuiz;
    private QuizSubmissionDTO submissionDTO;
    private QuizDTO quizDTO;

    @BeforeEach
    void setUp() {
        this.mockGuestQuizService = new GuestQuizServiceImpl(
                this.mockTempQuizStorage,
                this.mockGuestQuizStorage,
                this.mockCategoryStatsService,
                this.mockQuestionStatsService);

        this.mockQuiz = Quiz.builder()
                .viewToken("token123")
                .categoryId(10L)
                .categoryName("Math")
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .questions(List.of(
                        QuestionDTO.builder().id(1L).questionText("Q1").options(List.of("A","B")).correctAnswer("A").build(),
                        QuestionDTO.builder().id(2L).questionText("Q2").options(List.of("A","B")).correctAnswer("B").build()))
                .build();

        this.submissionDTO = QuizSubmissionDTO.builder()
                .viewToken("token123")
                .answers(Map.of(1L, "A", 2L, "X"))
                .build();

        this.quizDTO = QuizDTO.builder()
                .viewToken("token123")
                .correctAnswers(4)
                .totalQuestions(5)
                .scorePercent(80.0)
                .build();
    }

    @Test
    void evaluateQuiz_ShouldUpdateStats_RemoveTempQuiz_AndSaveResult() {
        when(this.mockTempQuizStorage.get("token123")).thenReturn(this.mockQuiz);

        this.mockGuestQuizService.evaluateQuiz(this.submissionDTO);

        verify(this.mockQuestionStatsService)
                .updateOnQuizCompleted(eq(this.mockQuiz), eq(this.submissionDTO.getAnswers()));
        verify(this.mockTempQuizStorage).remove("token123");
        verify(this.mockGuestQuizStorage).put(eq("token123"), any(QuizDTO.class));
        verify(this.mockCategoryStatsService)
                .updateOnQuizCompleted(eq(10L), eq(1), eq(2));
    }

    @Test
    void getQuizResult_ShouldReturnError_WhenQuizNotFound() {
        when(this.mockGuestQuizStorage.get("missing")).thenReturn(null);

        QuizNotFoundException exception = Assertions.assertThrows(QuizNotFoundException.class,
                () -> this.mockGuestQuizService.getQuizResult("missing"));

        Assertions.assertEquals("Куизът не е намерен.", exception.getMessage());
    }

    @Test
    void getQuizResult_ShouldReturnQuizResultDTO_WhenQuizExists() {
        when(this.mockGuestQuizStorage.get("token123")).thenReturn(this.quizDTO);

        QuizResultDTO result = this.mockGuestQuizService.getQuizResult("token123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.quizDTO.getViewToken(), result.getViewToken());
        Assertions.assertEquals(this.quizDTO.getCorrectAnswers(), result.getCorrectAnswers());
        Assertions.assertEquals(this.quizDTO.getTotalQuestions(), result.getTotalQuestions());
        Assertions.assertEquals(this.quizDTO.getScorePercent(), result.getScorePercent());
    }

    @Test
    void showQuizResult_ShouldReturnError_WhenQuizNotFound() {
        when(this.mockGuestQuizStorage.get("missing")).thenReturn(null);

        QuizNotFoundException exception = Assertions.assertThrows(QuizNotFoundException.class,
                () -> this.mockGuestQuizService.showQuizResult("missing"));

        Assertions.assertEquals("Куизът не е намерен.", exception.getMessage());
    }

    @Test
    void showQuizResult_ShouldReturnQuizDTO_WhenQuizExists() {
        when(this.mockGuestQuizStorage.get("token123")).thenReturn(this.quizDTO);

        QuizDTO result = this.mockGuestQuizService.showQuizResult("token123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.quizDTO.getViewToken(), result.getViewToken());
        Assertions.assertEquals(this.quizDTO.getCorrectAnswers(), result.getCorrectAnswers());
        Assertions.assertEquals(this.quizDTO.getTotalQuestions(), result.getTotalQuestions());
        Assertions.assertEquals(this.quizDTO.getScorePercent(), result.getScorePercent());
    }

    @Test
    void deleteExpiredGuestQuizzes_ShouldCallStorageAndReturnFormattedDate() {
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {

            mocked.when(LocalDateTime::now).thenReturn(fixedTime);

            String result = this.mockGuestQuizService.deleteExpiredGuestQuizzes();

            verify(this.mockGuestQuizStorage).deleteExpiredQuizzes(fixedTime);
            Assertions.assertEquals("01.01.2024 12:00:00", result);
        }
    }
}