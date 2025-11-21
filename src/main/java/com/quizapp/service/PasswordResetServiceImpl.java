package com.quizapp.service;

import com.quizapp.model.entity.PasswordResetToken;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;
import com.quizapp.repository.PasswordResetTokenRepository;
import com.quizapp.service.events.ForgotPasswordEvent;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public String createTokenForUser(User user) {

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        this.tokenRepository.saveAndFlush(resetToken);
        return token;
    }

    @Override
    public Result sendEmailForForgottenPassword(String email) {

        Optional<User> optionalUser = this.userService.getUserByEmail(email);
        if (optionalUser.isEmpty()) {
            return new Result(false, "Не открихме потребител с посочения имейл!");
        }

        User user = optionalUser.get();

        String token = this.tokenRepository.findByUserId(user.getId())
                .map(PasswordResetToken::getToken)
                .filter(this::isValidToken)
                .orElseGet(() -> this.createTokenForUser(user));

        this.applicationEventPublisher.publishEvent(
                new ForgotPasswordEvent(this, user.getUsername(), user.getEmail(), token));

        return new Result(true, "Моля проверете пощата си за имейл с линк за смяна на паролата!");
    }

    @Override
    public Result resetPassword(String password, String confirmPassword, String token) {

        if (!password.equals(confirmPassword)) {
            return new Result(false, "Паролите не съвпадат!");
        }

        Optional<PasswordResetToken> optionalToken = this.tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return new Result(false, "Невалиден линк за смяна на парола!");
        }

        PasswordResetToken passwordResetToken = optionalToken.get();

        if (!this.isValidToken(passwordResetToken.getToken())) {
            return new Result(false, "Този линк за смяна на паролата е изтекъл!");
        }

        User user = passwordResetToken.getUser();
        this.userService.resetUserPassword(user, password);

        passwordResetToken.setUsed(true);
        this.tokenRepository.save(passwordResetToken);

        return new Result(true, "Успешно променихте своята парола!");
    }

    @Override
    public boolean isValidToken(String token) {

        return this.tokenRepository.findByToken(token)
                .filter(resetToken ->
                        !resetToken.isUsed() && resetToken.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}