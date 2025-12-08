package com.quizapp.service;

import com.quizapp.model.dto.ResetPasswordDTO;
import com.quizapp.model.entity.PasswordResetToken;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.User;
import com.quizapp.repository.PasswordResetTokenRepository;
import com.quizapp.service.events.ForgotPasswordEvent;
import com.quizapp.service.interfaces.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceImplTest {

    @Mock
    private PasswordResetTokenRepository mockTokenRepository;

    @Mock
    private UserService mockUserService;

    @Mock
    private ApplicationEventPublisher mockAplEventPublisher;

    @InjectMocks
    private PasswordResetServiceImpl mockPasswordResetService;

    private User testUser;
    private PasswordResetToken resetToken;
    private ResetPasswordDTO mockResetPasswordDTO;

    @BeforeEach
    void setUp() {
        this.testUser = User.builder()
                .id(1L)
                .username("user")
                .email("user@gmail.com")
                .build();

        this.resetToken = PasswordResetToken.builder()
                .id(1L)
                .token("token123")
                .user(this.testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        this.mockResetPasswordDTO = ResetPasswordDTO.builder()
                .token("token123")
                .password("Password123")
                .confirmPassword("Password123")
                .build();
    }

    @Test
    void createTokenForUser_ShouldReturnTokenAndSave() {
        when(this.mockTokenRepository.saveAndFlush(any(PasswordResetToken.class))).thenReturn(this.resetToken);

        String token = this.mockPasswordResetService.createTokenForUser(this.testUser);

        Assertions.assertNotNull(token);
        verify(this.mockTokenRepository, times(1)).saveAndFlush(any(PasswordResetToken.class));
    }

    @Test
    void sendEmailForForgottenPassword_ShouldReturnError_WhenUserNotFound() {
        when(this.mockUserService.getUserByEmail("user_not_found@gmail.com")).thenReturn(Optional.empty());

        Result result = this.mockPasswordResetService.sendEmailForForgottenPassword("user_not_found@gmail.com");

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Не открихме потребител с посочения имейл!", result.getMessage());
    }

    @Test
    void sendEmailForForgottenPassword_ShouldSendEmail_WhenUserExistsAndValidToken() {
        when(this.mockUserService.getUserByEmail(this.testUser.getEmail())).thenReturn(Optional.of(this.testUser));
        when(this.mockTokenRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.of(this.resetToken));

        Result result = this.mockPasswordResetService.sendEmailForForgottenPassword(this.testUser.getEmail());

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Моля проверете пощата си за имейл с линк за смяна на паролата!", result.getMessage());
        verify(this.mockAplEventPublisher, times(1)).publishEvent(any(ForgotPasswordEvent.class));
    }

    @Test
    void sendEmailForForgottenPassword_ShouldCreateNewToken_WhenTokenInvalid() {
        when(this.mockUserService.getUserByEmail(this.testUser.getEmail())).thenReturn(Optional.of(this.testUser));
        when(this.mockTokenRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.empty());
        when(this.mockTokenRepository.saveAndFlush(any(PasswordResetToken.class))).thenReturn(this.resetToken);

        Result result = this.mockPasswordResetService.sendEmailForForgottenPassword(this.testUser.getEmail());

        Assertions.assertTrue(result.isSuccess());
        verify(this.mockAplEventPublisher, times(1)).publishEvent(any(ForgotPasswordEvent.class));
        verify(this.mockTokenRepository, times(1)).saveAndFlush(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_ShouldReturnError_WhenDtoIsNull() {
        Result result = this.mockPasswordResetService.resetPassword(null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Невалидни входни данни!", result.getMessage());
    }

    @Test
    void resetPassword_ShouldReturnError_WhenPasswordsDoNotMatch() {
        this.mockResetPasswordDTO.setConfirmPassword("WrongPassword");

        Result result = this.mockPasswordResetService.resetPassword(this.mockResetPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Паролите не съвпадат!", result.getMessage());
    }

    @Test
    void resetPassword_ShouldReturnError_WhenTokenNotFound() {
        when(this.mockTokenRepository.findByToken(this.resetToken.getToken())).thenReturn(Optional.empty());

        Result result = this.mockPasswordResetService.resetPassword(this.mockResetPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Невалиден линк за смяна на парола!", result.getMessage());
    }

    @Test
    void resetPassword_ShouldReturnError_WhenTokenInvalid() {
        this.resetToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(this.mockTokenRepository.findByToken(this.resetToken.getToken())).thenReturn(Optional.of(this.resetToken));

        Result result = this.mockPasswordResetService.resetPassword(this.mockResetPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Този линк за смяна на паролата е изтекъл или е използван!", result.getMessage());
    }

    @Test
    void resetPassword_ShouldResetPassword_WhenUserFoundAndTokenIsValid() {
        when(this.mockTokenRepository.findByToken(this.resetToken.getToken())).thenReturn(Optional.of(this.resetToken));

        Result result = this.mockPasswordResetService.resetPassword(this.mockResetPasswordDTO);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно променихте своята парола!", result.getMessage());
        verify(this.mockUserService, times(1)).resetUserPassword(this.testUser, this.mockResetPasswordDTO.getPassword());
        Assertions.assertTrue(this.resetToken.isUsed());
        verify(this.mockTokenRepository, times(1)).save(this.resetToken);
    }
}