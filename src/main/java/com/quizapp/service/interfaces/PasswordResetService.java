package com.quizapp.service.interfaces;

import com.quizapp.model.dto.ResetPasswordDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;

import java.time.LocalDateTime;

public interface PasswordResetService {

    Result resetPassword(ResetPasswordDTO resetPasswordDTO);

    String createTokenForUser(User user);

    Result sendEmailForForgottenPassword(String email);

    boolean isValidToken(String token);

    void deleteInvalidPasswordResetTokens(LocalDateTime now);
}