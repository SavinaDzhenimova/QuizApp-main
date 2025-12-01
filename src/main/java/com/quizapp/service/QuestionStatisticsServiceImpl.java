package com.quizapp.service;

import com.quizapp.exception.QuestionStatisticsNotFound;
import com.quizapp.model.entity.QuestionStatistics;
import com.quizapp.model.entity.Quiz;
import com.quizapp.repository.QuestionStatisticsRepository;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    @Override
    public void updateOnQuizCompleted(Quiz quiz, Map<Long, String> userAnswers) {
        quiz.getQuestions().forEach(questionDTO -> {
            QuestionStatistics stats = this.questionStatisticsRepository
                    .findByQuestionId(questionDTO.getId())
                    .orElseThrow(() -> new QuestionStatisticsNotFound("Не е намерена статистика за този въпрос."));

            boolean isCorrect = questionDTO.getCorrectAnswer().equals(userAnswers.get(questionDTO.getId()));

            if (isCorrect) {
                stats.setCorrectAnswers(stats.getCorrectAnswers() + 1);
            } else {
                stats.setWrongAnswers(stats.getWrongAnswers() + 1);
            }

            int correctAnswers = stats.getCorrectAnswers();
            int wrongAnswers = stats.getWrongAnswers();
            int completed = correctAnswers + wrongAnswers;
            int attempts = stats.getAttempts();

            stats.setAccuracy((correctAnswers * 100.00) / attempts);
            stats.setDifficulty((wrongAnswers * 100.00) / attempts);

            if (attempts > 0) {
                double completionRate = (completed * 100.00) / attempts;
                stats.setCompletionRate(completionRate);
            }

            this.questionStatisticsRepository.saveAndFlush(stats);
        });
    }
}