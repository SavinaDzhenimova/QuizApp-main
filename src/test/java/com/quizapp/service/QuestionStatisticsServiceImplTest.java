package com.quizapp.service;

import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.dto.question.QuestionStatsDTO;
import com.quizapp.model.entity.CategoryStatistics;
import com.quizapp.model.entity.QuestionStatistics;
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

import java.util.List;

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
    }

    @Test
    void getFilteredQuestionStatistics_ShouldReturnPageQuestionStatsDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        String questionText = "Question";

        Page<QuestionStatistics> page = new PageImpl<>(List.of(this.mockQuestionStats));

        ArgumentCaptor<Specification<QuestionStatistics>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        when(this.mockQuestionStatsRepository.findAll(ArgumentMatchers.<Specification<QuestionStatistics>>any(), eq(pageable))).thenReturn(page);

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
}