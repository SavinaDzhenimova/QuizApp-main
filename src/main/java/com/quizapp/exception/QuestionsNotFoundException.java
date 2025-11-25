package com.quizapp.exception;

public class QuestionsNotFoundException extends RuntimeException {

    public QuestionsNotFoundException(String message) {
        super(message);
    }
}