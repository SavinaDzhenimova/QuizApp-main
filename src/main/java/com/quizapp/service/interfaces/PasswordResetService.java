package com.quizapp.service.interfaces;

import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;

public interface PasswordResetService {

    Result resetPassword(String password, String confirmPassword, String token);

    String createTokenForUser(User user);

    Result sendEmailForForgottenPassword(String email);

    boolean isValidToken(String token);
}