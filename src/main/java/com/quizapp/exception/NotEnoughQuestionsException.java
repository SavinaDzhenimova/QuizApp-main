package com.quizapp.exception;

public class NotEnoughQuestionsException extends RuntimeException {

    public NotEnoughQuestionsException(String message) {
        super(message);
    }
}