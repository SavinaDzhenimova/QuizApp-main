package com.quizapp.exception;

public class InvalidPasswordResetToken extends RuntimeException {

    public InvalidPasswordResetToken(String message) {
        super(message);
    }
}
