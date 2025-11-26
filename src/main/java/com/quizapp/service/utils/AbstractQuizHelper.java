package com.quizapp.service.utils;

import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.entity.Quiz;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractQuizHelper {

    private final TempQuizStorage tempQuizStorage;

    protected Quiz loadTempQuiz(String viewToken) {
        Quiz quiz = this.tempQuizStorage.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quiz;
    }

    protected void removeTempQuiz(String viewToken) {
        this.tempQuizStorage.remove(viewToken);
    }

    protected void putTempQuiz(String viewToken, Quiz quiz) {
        this.tempQuizStorage.put(viewToken, quiz);
    }

    protected Long getCorrectAnswers(Quiz quiz, Map<Long, String> userAnswers) {
        return quiz.getQuestions().stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();
    }

    protected Map<Long, String> mapUserAnswers(Map<String, String> formData) {
        Map<Long, String> userAnswers = new HashMap<>();

        formData.forEach((key, value) -> {
            if (key.startsWith("answers[")) {
                Long questionId = Long.valueOf(key.replaceAll("[^0-9]", ""));
                userAnswers.put(questionId, value);
            }
        });

        return userAnswers;
    }
}