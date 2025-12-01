package com.quizapp.service.interfaces;

import com.quizapp.model.dto.question.QuestionStatsDTO;
import com.quizapp.model.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface QuestionStatisticsService {

    Page<QuestionStatsDTO> getAllQuestionStatsForChartsByCategoryId(Long categoryId, Pageable pageable);

    void increaseUsedQuestion(Long questionId, String questionText, Long categoryId);

    void updateOnQuizCompleted(Quiz quiz, Map<Long, String> userAnswers);
}