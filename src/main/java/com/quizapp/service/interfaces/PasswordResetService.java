package com.quizapp.service.interfaces;

import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;
import jakarta.transaction.Transactional;

import java.util.Optional;

public interface PasswordResetService {

    void createTokenForUser(User user, String token);

    Result resetPassword(String password, String confirmPassword, String token);

    Result sendEmailForForgottenPassword(String email);

    Optional<User> validateToken(String token);

    @Transactional
    void markTokenAsUsed(String token);
}