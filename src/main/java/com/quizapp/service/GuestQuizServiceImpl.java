package com.quizapp.service;

import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.utils.AbstractQuizHelper;
import com.quizapp.service.utils.TempQuizStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GuestQuizServiceImpl extends AbstractQuizHelper implements GuestQuizService {

    private final Map<String, QuizDTO> guestQuizResults = new ConcurrentHashMap<>();

    public GuestQuizServiceImpl(TempQuizStorage tempQuizStorage) {
        super(tempQuizStorage);
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
        QuizDTO quizDTO = this.guestQuizResults.get(viewToken);

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
        long correctAnswers = this.getCorrectAnswers(quiz, userAnswers);
        double scorePercent = ((double) correctAnswers / totalQuestions) * 100;

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

        this.guestQuizResults.put(quiz.getViewToken(), quizDTO);
    }

    @Override
    public QuizDTO showQuizResult(String viewToken) {
        QuizDTO quizDTO = this.guestQuizResults.get(viewToken);

        if (quizDTO == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quizDTO;
    }

    @Override
    public void deleteExpiredGuestQuizzes(LocalDateTime dateTime) {
        this.guestQuizResults.entrySet()
                .removeIf(entry -> entry.getValue().getExpireAt().isBefore(dateTime));
    }
}