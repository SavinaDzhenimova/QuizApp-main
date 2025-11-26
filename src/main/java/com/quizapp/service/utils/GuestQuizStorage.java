package com.quizapp.service.utils;

import com.quizapp.model.dto.quiz.QuizDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GuestQuizStorage {

    private final Map<String, QuizDTO> guestQuizResults = new ConcurrentHashMap<>();

    public QuizDTO get(String viewToken) {
        return this.guestQuizResults.get(viewToken);
    }

    public void remove(String viewToken) {
        this.guestQuizResults.remove(viewToken);
    }

    public void put(String viewToken, QuizDTO quizDTO) {
        this.guestQuizResults.put(viewToken, quizDTO);
    }

    public void deleteExpiredQuizzes(LocalDateTime dateTime) {
        this.guestQuizResults.entrySet()
                .removeIf(entry -> entry.getValue().getExpireAt().isBefore(dateTime));
    }
}