package com.quizapp.service;

import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.utils.AbstractQuizHelper;
import com.quizapp.service.utils.GuestQuizStorage;
import com.quizapp.service.utils.TempQuizStorage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GuestQuizServiceImpl extends AbstractQuizHelper implements GuestQuizService {

    private final GuestQuizStorage guestQuizStorage;
    private final CategoryStatisticsService categoryStatisticsService;

    public GuestQuizServiceImpl(TempQuizStorage tempQuizStorage, GuestQuizStorage guestQuizStorage,
                                CategoryStatisticsService categoryStatisticsService) {
        super(tempQuizStorage);
        this.guestQuizStorage = guestQuizStorage;
        this.categoryStatisticsService = categoryStatisticsService;
    }

    @Override
    public void evaluateQuiz(String viewToken, Map<String, String> formData) {
        Quiz quiz = super.loadTempQuiz(viewToken);

        Map<Long, String> userAnswers = super.mapUserAnswers(formData);

        super.removeTempQuiz(viewToken);

        this.saveQuizResult(quiz, userAnswers);
    }

    @Override
    public QuizResultDTO getQuizResult(String viewToken) {
        QuizDTO quizDTO = this.guestQuizStorage.get(viewToken);

        if (quizDTO == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return QuizResultDTO.builder()
                .viewToken(viewToken)
                .correctAnswers(quizDTO.getCorrectAnswers())
                .totalQuestions(quizDTO.getTotalQuestions())
                .scorePercent(quizDTO.getScorePercent())
                .build();
    }

    private void saveQuizResult(Quiz quiz, Map<Long, String> userAnswers) {

        int totalQuestions = quiz.getQuestions().size();
        long correctAnswers = super.getCorrectAnswers(quiz, userAnswers);
        double scorePercent = ((double) correctAnswers / totalQuestions) * 100;

        this.categoryStatisticsService.updateOnQuizCompleted(quiz.getCategoryId(), (int) correctAnswers, totalQuestions);

        QuizDTO quizDTO = QuizDTO.builder()
                .viewToken(quiz.getViewToken())
                .correctAnswers((int) correctAnswers)
                .totalQuestions(totalQuestions)
                .scorePercent(scorePercent)
                .categoryName(quiz.getCategoryName())
                .solvedAt(LocalDateTime.now())
                .expireAt(quiz.getExpireAt())
                .questions(quiz.getQuestions())
                .userAnswers(userAnswers)
                .build();

        this.guestQuizStorage.put(quiz.getViewToken(), quizDTO);
    }

    @Override
    public QuizDTO showQuizResult(String viewToken) {
        QuizDTO quizDTO = this.guestQuizStorage.get(viewToken);

        if (quizDTO == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quizDTO;
    }

    @Override
    public void deleteExpiredGuestQuizzes(LocalDateTime dateTime) {
        this.guestQuizStorage.deleteExpiredQuizzes(dateTime);
    }
}