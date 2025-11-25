package com.quizapp.exception;

public class NoQuestionsFoundException extends RuntimeException {

    public NoQuestionsFoundException(String message) {
        super(message);
    }
}