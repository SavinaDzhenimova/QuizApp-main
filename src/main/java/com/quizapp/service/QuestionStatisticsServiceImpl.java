package com.quizapp.service;

import com.quizapp.model.entity.QuestionStatistics;
import com.quizapp.repository.QuestionStatisticsRepository;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionStatisticsServiceImpl implements QuestionStatisticsService {

    private final QuestionStatisticsRepository questionStatisticsRepository;
    private final QuestionService questionService;

    @Override
    public void increaseUsedQuestion(Long questionId, String questionText) {
        QuestionStatistics questionStatistics = this.questionStatisticsRepository.findByQuestionId(questionId)
                .orElseGet(() -> this.createNewStatistics(questionId, questionText));

        questionStatistics.setAttempts(questionStatistics.getAttempts() + 1);

        int completed = questionStatistics.getCorrectAnswers() + questionStatistics.getWrongAnswers();
        int attempts = questionStatistics.getAttempts();
        if (attempts > 0) {
            questionStatistics.setCompletionRate((completed * 100.0) / attempts);
        }

        this.questionStatisticsRepository.saveAndFlush(questionStatistics);
    }

    private QuestionStatistics createNewStatistics(Long questionId, String questionText) {
        return QuestionStatistics.builder()
                .questionId(questionId)
                .questionText(questionText)
                .attempts(0)
                .correctAnswers(0)
                .wrongAnswers(0)
                .accuracy(0)
                .difficulty(0)
                .build();
    }
}