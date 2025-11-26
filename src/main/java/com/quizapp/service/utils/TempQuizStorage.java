package com.quizapp.service.utils;

import com.quizapp.model.entity.Quiz;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TempQuizStorage {

    private final Map<String, Quiz> tempQuizzes = new ConcurrentHashMap<>();

    public Quiz get(String viewToken) {
        return this.tempQuizzes.get(viewToken);
    }

    public void remove(String viewToken) {
        this.tempQuizzes.remove(viewToken);
    }

    public void put(String viewToken, Quiz quiz) {
        this.tempQuizzes.put(viewToken, quiz);
    }
}