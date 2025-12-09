package com.quizapp.service;

import com.quizapp.exception.QuestionStatisticsNotFound;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.question.QuestionStatsDTO;
import com.quizapp.model.entity.QuestionStatistics;
import com.quizapp.model.entity.Quiz;
import com.quizapp.repository.QuestionStatisticsRepository;
import com.quizapp.service.interfaces.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionStatisticsServiceImplTest {

    @Mock
    private QuestionStatisticsRepository mockQuestionStatsRepository;
    @Mock
    private CategoryService mockCategoryService;
    @InjectMocks
    private QuestionStatisticsServiceImpl mockQuestionStatsService;

    private QuestionStatistics mockQuestionStats;
    private QuestionDTO questionDTO;
    private Quiz mockQuiz;

    @BeforeEach
    void setUp() {
        this.mockQuestionStats = QuestionStatistics.builder()
                .questionId(1L)
                .categoryId(1L)
                .questionText("Question")
                .attempts(20)
                .correctAnswers(15)
                .wrongAnswers(2)
                .accuracy(75.00)
                .completionRate(90.00)
                .build();

        this.questionDTO = QuestionDTO.builder()
                .id(1L)
                .categoryName("Category")
                .questionText("Question")
                .correctAnswer("A")
                .options(List.of("A", "B", "C", "D"))
                .build();

        this.mockQuiz = Quiz.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Category")
                .questions(List.of(this.questionDTO))
                .viewToken("token123")
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    void getFilteredQuestionStatistics_ShouldCallRepositoryWithSpec() {
        Long categoryId = 5L;
        String text = "capital";
        Pageable pageable = PageRequest.of(0, 10);

        ArgumentCaptor<Specification<QuestionStatistics>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        when(this.mockQuestionStatsRepository.findAll(ArgumentMatchers.<Specification<QuestionStatistics>>any(), eq(pageable)))
                .thenReturn(Page.empty());

        this.mockQuestionStatsService.getFilteredQuestionStatistics(categoryId, text, pageable);

        verify(this.mockQuestionStatsRepository).findAll(specCaptor.capture(), eq(pageable));
        Specification<QuestionStatistics> usedSpec = specCaptor.getValue();

        Assertions.assertNotNull(usedSpec);
    }

    @Test
    void getFilteredQuestionStatistics_ShouldReturnPageQuestionStatsDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        String questionText = "Question";

        Page<QuestionStatistics> page = new PageImpl<>(List.of(this.mockQuestionStats));

        ArgumentCaptor<Specification<QuestionStatistics>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        when(this.mockQuestionStatsRepository.findAll(ArgumentMatchers.<Specification<QuestionStatistics>>any(), eq(pageable)))
                .thenReturn(page);

        Page<QuestionStatsDTO> result = this.mockQuestionStatsService.getFilteredQuestionStatistics(1L, questionText, pageable);

        verify(this.mockQuestionStatsRepository, times(1)).findAll(specCaptor.capture(), eq(pageable));

        Assertions.assertNotNull(specCaptor.getValue());
        Assertions.assertEquals(1, result.getTotalElements());

        QuestionStatsDTO dto = result.getContent().get(0);

        Assertions.assertEquals(this.mockQuestionStats.getCategoryId(), dto.getCategoryId());
        Assertions.assertEquals(this.mockQuestionStats.getQuestionId(), dto.getQuestionId());
        Assertions.assertEquals(this.mockQuestionStats.getQuestionText(), dto.getQuestionText());
        Assertions.assertEquals(this.mockQuestionStats.getCorrectAnswers(), dto.getCorrectAnswers());
        Assertions.assertEquals(this.mockQuestionStats.getWrongAnswers(), dto.getWrongAnswers());
        Assertions.assertEquals(this.mockQuestionStats.getAttempts(), dto.getAttempts());
        Assertions.assertEquals(this.mockQuestionStats.getAccuracy(), dto.getAccuracy());
        Assertions.assertEquals(this.mockQuestionStats.getCompletionRate(), dto.getCompletionRate());
    }

    @Test
    void increaseUsedQuestion_ShouldCreateNewStatistics_WhenNotExist() {
        String questionText = "Question";
        when(this.mockQuestionStatsRepository.findByQuestionId(5L))
                .thenReturn(Optional.empty());

        this.mockQuestionStatsService.increaseUsedQuestion(5L, questionText, 1L);

        ArgumentCaptor<QuestionStatistics> statsCaptor = ArgumentCaptor.forClass(QuestionStatistics.class);

        verify(this.mockQuestionStatsRepository).saveAndFlush(statsCaptor.capture());
        QuestionStatistics savedStats = statsCaptor.getValue();

        Assertions.assertEquals(5L, savedStats.getQuestionId());
        Assertions.assertEquals(1L, savedStats.getCategoryId());
        Assertions.assertEquals(questionText, savedStats.getQuestionText());
        Assertions.assertEquals(1, savedStats.getAttempts());
        Assertions.assertEquals(0, savedStats.getCompletionRate());
    }

    @Test
    void increaseUsedQuestion_ShouldIncreaseAttemptsAndRecalculateRate_WhenNotExist() {
        String questionText = "Question";
        when(this.mockQuestionStatsRepository.findByQuestionId(1L))
                .thenReturn(Optional.of(this.mockQuestionStats));

        this.mockQuestionStatsService.increaseUsedQuestion(1L, questionText, 1L);

        Assertions.assertEquals(21, this.mockQuestionStats.getAttempts());

        double completionRate = (this.mockQuestionStats.getCorrectAnswers() + this.mockQuestionStats.getWrongAnswers()) * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(completionRate, this.mockQuestionStats.getCompletionRate());
        verify(this.mockQuestionStatsRepository, times(1)).saveAndFlush(this.mockQuestionStats);
    }

    @Test
    void updateOnQuizCompleted_ShouldThrowException_WhenQuestionStatsNotFound() {
        when(this.mockQuestionStatsRepository.findByQuestionId(1L))
                .thenReturn(Optional.empty());

        Map<Long, String> answers = Map.of(1L, "A");

        QuestionStatisticsNotFound exception = Assertions.assertThrows(QuestionStatisticsNotFound.class,
                () -> this.mockQuestionStatsService.updateOnQuizCompleted(this.mockQuiz, answers));

        Assertions.assertEquals("Не е намерена статистика за този въпрос.", exception.getMessage());
        verify(this.mockQuestionStatsRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateOnQuizCompleted_ShouldUpdateQuestionStats_WhenWrongAnswer() {
        when(this.mockQuestionStatsRepository.findByQuestionId(1L))
                .thenReturn(Optional.of(this.mockQuestionStats));

        Map<Long, String> answers = Map.of(1L, "C");

        this.mockQuestionStatsService.updateOnQuizCompleted(this.mockQuiz, answers);

        Assertions.assertEquals(15, this.mockQuestionStats.getCorrectAnswers());
        Assertions.assertEquals(3, this.mockQuestionStats.getWrongAnswers());

        double accuracy = this.mockQuestionStats.getCorrectAnswers() * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(accuracy, this.mockQuestionStats.getAccuracy());

        double difficulty = this.mockQuestionStats.getWrongAnswers() * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(difficulty, this.mockQuestionStats.getDifficulty());

        double completionRate = (this.mockQuestionStats.getCorrectAnswers() + this.mockQuestionStats.getWrongAnswers()) * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(completionRate, this.mockQuestionStats.getCompletionRate());

        verify(this.mockQuestionStatsRepository, times(1)).saveAndFlush(this.mockQuestionStats);
    }

    @Test
    void updateOnQuizCompleted_ShouldUpdateQuestionStats_WhenCorrectAnswer() {
        when(this.mockQuestionStatsRepository.findByQuestionId(1L))
                .thenReturn(Optional.of(this.mockQuestionStats));

        Map<Long, String> answers = Map.of(1L, "A");

        this.mockQuestionStatsService.updateOnQuizCompleted(this.mockQuiz, answers);

        Assertions.assertEquals(16, this.mockQuestionStats.getCorrectAnswers());
        Assertions.assertEquals(2, this.mockQuestionStats.getWrongAnswers());

        double accuracy = this.mockQuestionStats.getCorrectAnswers() * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(accuracy, this.mockQuestionStats.getAccuracy());

        double difficulty = this.mockQuestionStats.getWrongAnswers() * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(difficulty, this.mockQuestionStats.getDifficulty());

        double completionRate = (this.mockQuestionStats.getCorrectAnswers() + this.mockQuestionStats.getWrongAnswers()) * 100.00 / this.mockQuestionStats.getAttempts();
        Assertions.assertEquals(completionRate, this.mockQuestionStats.getCompletionRate());

        verify(this.mockQuestionStatsRepository, times(1)).saveAndFlush(this.mockQuestionStats);
    }
}