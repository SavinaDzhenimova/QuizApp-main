package com.quizapp.service;

import com.quizapp.model.entity.PasswordResetToken;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;
import com.quizapp.repository.PasswordResetTokenRepository;
import com.quizapp.service.events.ForgotPasswordEvent;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.UserService;
import jakarta.transaction.Transactional;
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
    public void createTokenForUser(User user, String token) {

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        this.tokenRepository.saveAndFlush(resetToken);
    }

    @Override
    public Result resetPassword(String password, String confirmPassword, String token) {

        Optional<User> optionalUser = this.validateToken(token);

        if (optionalUser.isEmpty()) {
            return new Result(false, "Паролите не съвпадат!");
        }

        User user = optionalUser.get();

        this.userService.resetUserPassword(user, password);

        return new Result(true, "Успешно променихте своята парола!");
    }

    @Override
    public Result sendEmailForForgottenPassword(String email) {

        Optional<User> optionalUser = this.userService.getUserByEmail(email);

        if (optionalUser.isEmpty()) {
            return new Result(false, "Не открихме потребител с посочения имейл!");
        }

        User user = optionalUser.get();

        String token = UUID.randomUUID().toString();
        this.createTokenForUser(user, token);

        this.applicationEventPublisher.publishEvent(
                new ForgotPasswordEvent(this, user.getUsername(), user.getEmail(), token));

        return new Result(true, "Моля проверете пощата си за имейл с линк за смяна на паролата!");
    }

    @Override
    public Optional<User> validateToken(String token) {

        return this.tokenRepository.findByToken(token)
                .filter(resetToken ->
                        !resetToken.isUsed() && resetToken.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(PasswordResetToken::getUser);
    }

    @Override
    @Transactional
    public void markTokenAsUsed(String token) {

        this.tokenRepository.findByToken(token).ifPresent(resetToken -> {
            resetToken.setUsed(true);
            this.tokenRepository.saveAndFlush(resetToken);
        });

        this.tokenRepository.deleteByToken(token);
    }
}