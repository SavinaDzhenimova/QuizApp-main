package com.quizapp.service;

import com.quizapp.exception.UserNotFoundException;
import com.quizapp.model.dto.user.UpdatePasswordDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.events.UserRegisterEvent;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.RoleService;
import com.quizapp.service.interfaces.UserStatisticsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private CategoryService mockCategoryService;
    @Mock
    private RoleService mockRoleService;
    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @Mock
    private ApplicationEventPublisher mockAplEventPublisher;
    @Mock
    private UserStatisticsService mockUserStatisticsService;
    @InjectMocks
    private UserServiceImpl mockUserService;

    private Role roleUser;
    private User testUser;
    private UserStatistics userStatistics;
    private UserRegisterDTO mockRegisterDTO;
    private UpdatePasswordDTO mockPasswordDTO;

    @BeforeEach
    void setUp() {
        this.roleUser = new Role();
        this.roleUser.setName(RoleName.USER);

        this.userStatistics = UserStatistics.builder()
                .totalQuizzes(5)
                .totalCorrectAnswers(40)
                .maxScore(50)
                .averageScore(8.0)
                .lastSolvedAt(LocalDateTime.now())
                .build();

        this.testUser = User.builder()
                .id(1L)
                .username("user")
                .email("user@gmail.com")
                .password("Password123")
                .roles(new HashSet<>(Set.of(this.roleUser)))
                .userStatistics(this.userStatistics)
                .solvedQuizzes(new ArrayList<>())
                .build();

        this.mockRegisterDTO = UserRegisterDTO.builder()
                .username("user")
                .email("user@gmail.com")
                .password("Password123")
                .confirmPassword("Password123")
                .build();

        this.mockPasswordDTO = UpdatePasswordDTO.builder()
                .oldPassword("Password123")
                .newPassword("Pass123")
                .confirmPassword("Pass123")
                .build();
    }

    @Test
    void getUserInfo_ShouldThrowUserNotFoundError_WhenUserNotFound() {
        when(this.mockUserRepository.findByUsername("non_existent")).thenReturn(Optional.empty());

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
                () -> this.mockUserService.getUserInfo("non_existent"));

        Assertions.assertEquals("Не е намерен потребител", exception.getMessage());
    }

    @Test
    void getUserInfo_ShouldReturnUserDTO_WithSolvedQuizzesAndStats() {
        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.of(this.testUser));

        UserDTO userDTO = this.mockUserService.getUserInfo(this.testUser.getUsername());

        Assertions.assertNotNull(userDTO);
        Assertions.assertEquals(this.testUser.getUsername(), userDTO.getUsername());
        Assertions.assertEquals(this.testUser.getEmail(), userDTO.getEmail());
        Assertions.assertNotNull(userDTO.getUserStats());
        Assertions.assertNotNull(userDTO.getSolvedQuizzes());
        Assertions.assertEquals(this.userStatistics.getTotalQuizzes(), userDTO.getUserStats().getTotalQuizzes());
    }

    @Test
    void registerUser_ShouldReturnError_WhenDtoIsNull() {
        Result result = this.mockUserService.registerUser(null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Не е намерен потребител!", result.getMessage());
    }

    @Test
    void registerUser_ShouldReturnError_WhenPasswordsDoNotMatch() {
        this.mockRegisterDTO.setConfirmPassword("Pass123");

        Result result = this.mockUserService.registerUser(this.mockRegisterDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Паролите не съвпадат!", result.getMessage());
    }

    @Test
    void registerUser_ShouldReturnError_WhenUserExistsByUsername() {
        when(this.mockUserRepository.existsByUsername(this.testUser.getUsername())).thenReturn(true);

        Result result = this.mockUserService.registerUser(this.mockRegisterDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Вече съществува потребител с това потребителско име!", result.getMessage());
    }

    @Test
    void registerUser_ShouldReturnError_WhenUserExistsByEmail() {
        when(this.mockUserRepository.existsByUsername(this.testUser.getUsername())).thenReturn(false);
        when(this.mockUserRepository.existsByEmail(this.testUser.getEmail())).thenReturn(true);

        Result result = this.mockUserService.registerUser(this.mockRegisterDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Вече съществува потребител с този имейл!", result.getMessage());
    }

    @Test
    void registerUser_ShouldReturnError_WhenRoleUserNotFound() {
        when(this.mockUserRepository.existsByUsername(this.testUser.getUsername())).thenReturn(false);
        when(this.mockUserRepository.existsByEmail(this.testUser.getEmail())).thenReturn(false);
        when(this.mockRoleService.getRoleByName(RoleName.USER)).thenReturn(Optional.empty());

        Result result = this.mockUserService.registerUser(this.mockRegisterDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Ролята User не съществува!", result.getMessage());
    }

    @Test
    void registerUser_ShouldSaveUser_WhenDataIsValid() {
        when(this.mockUserRepository.existsByUsername(this.testUser.getUsername())).thenReturn(false);
        when(this.mockUserRepository.existsByEmail(this.testUser.getEmail())).thenReturn(false);
        when(this.mockRoleService.getRoleByName(RoleName.USER)).thenReturn(Optional.of(this.roleUser));
        when(this.mockPasswordEncoder.encode(this.testUser.getPassword())).thenReturn("encodedPass");
        when(this.mockUserRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(this.mockUserStatisticsService.createInitialStatistics(any(User.class)))
                .thenAnswer(inv -> UserStatistics.builder().user(inv.getArgument(0)).build());

        Result result = this.mockUserService.registerUser(this.mockRegisterDTO);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешна регистрация!", result.getMessage());

        ArgumentCaptor<UserRegisterEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisterEvent.class);
        verify(this.mockAplEventPublisher, times(1)).publishEvent(eventCaptor.capture());

        UserRegisterEvent publishedEvent = eventCaptor.getValue();

        Assertions.assertEquals(this.testUser.getUsername(), publishedEvent.getUsername());
        Assertions.assertEquals(this.testUser.getEmail(), publishedEvent.getEmail());
    }

    @Test
    void updatePassword_ShouldReturnError_WhenDtoIsNull() {
        Result result = this.mockUserService.updatePassword(this.testUser.getUsername(), null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Невалидни входни данни!", result.getMessage());
    }

    @Test
    void updatePassword_ShouldReturnError_WhenUserNotFound() {
        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.empty());

        Result result = this.mockUserService.updatePassword(this.testUser.getUsername(), this.mockPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Потребителят не е намерен!", result.getMessage());
    }

    @Test
    void updatePassword_ShouldReturnError_WhenPasswordsDoNotMatch() {
        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.of(this.testUser));
        this.mockPasswordDTO.setConfirmPassword("WrongPassword");

        Result result = this.mockUserService.updatePassword(this.testUser.getUsername(), this.mockPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Паролите не съвпадат!", result.getMessage());
    }

    @Test
    void updatePassword_ShouldReturnError_WhenOldPasswordIsWrong() {
        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.of(this.testUser));
        this.mockPasswordDTO.setOldPassword("WrongPassword");

        Result result = this.mockUserService.updatePassword(this.testUser.getUsername(), this.mockPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Старата парола е грешна!", result.getMessage());
    }

    @Test
    void updatePassword_ShouldReturnError_WhenNewEqualsOldPassword() {
        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.of(this.testUser));
        this.mockPasswordDTO.setOldPassword("Password123");
        this.mockPasswordDTO.setNewPassword("Password123");
        this.mockPasswordDTO.setConfirmPassword("Password123");
        when(this.mockPasswordEncoder.matches(this.mockPasswordDTO.getOldPassword(), this.mockPasswordDTO.getNewPassword())).thenReturn(true);

        Result result = this.mockUserService.updatePassword(this.testUser.getUsername(), this.mockPasswordDTO);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Новата парола не може да е като старата парола!", result.getMessage());
    }

    @Test
    void updatePassword_ShouldUpdatePassword_WhenDtoIsValid() {
        this.testUser.setPassword("encodedOldPassword");

        when(mockUserRepository.findByUsername("user")).thenReturn(Optional.of(this.testUser));
        when(mockPasswordEncoder.matches("Password123", "encodedOldPassword")).thenReturn(true);
        when(mockPasswordEncoder.matches("Pass123", "encodedOldPassword")).thenReturn(false);
        when(mockPasswordEncoder.encode("Pass123")).thenReturn("encodedNewPassword");
        when(mockUserRepository.saveAndFlush(this.testUser)).thenReturn(this.testUser);

        Result result = mockUserService.updatePassword("user", mockPasswordDTO);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно променихте паролата си.", result.getMessage());
        Assertions.assertEquals("encodedNewPassword", testUser.getPassword());
        verify(this.mockUserRepository, times(1)).saveAndFlush(this.testUser);
    }

    @Test
    void resetUserPassword_ShouldEncodeAndSavePassword() {
        this.testUser.setPassword("oldPassword");
        String newPassword = "newPassword";

        when(this.mockPasswordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(this.mockUserRepository.saveAndFlush(this.testUser)).thenReturn(this.testUser);

        this.mockUserService.resetUserPassword(this.testUser, newPassword);

        Assertions.assertEquals("encodedNewPassword", this.testUser.getPassword());
        verify(this.mockPasswordEncoder, times(1)).encode(newPassword);
        verify(this.mockUserRepository, times(1)).saveAndFlush(this.testUser);
    }

    @Test
    void updateLastLoginTime_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(this.mockUserRepository.findByUsername("non_existent")).thenReturn(Optional.empty());

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
                () -> this.mockUserService.updateLastLoginTime("non_existent"));

        Assertions.assertEquals("Не е намерен потребител non_existent.", exception.getMessage());
    }

    @Test
    void updateLastLoginTime_ShouldNotDoAnything_WhenUserStatisticsNull() {
        this.testUser.setUserStatistics(null);
        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.of(this.testUser));

        this.mockUserService.updateLastLoginTime(this.testUser.getUsername());

        Assertions.assertNull(this.testUser.getUserStatistics());
        verify(this.mockUserRepository, never()).saveAndFlush(this.testUser);
    }

    @Test
    void updateLastLoginTime_ShouldUpdateTimestamp_WhenUserExists() {
        this.userStatistics.setLastLoginAt(LocalDateTime.now().minusDays(1));
        this.testUser.setUserStatistics(this.userStatistics);

        when(this.mockUserRepository.findByUsername(this.testUser.getUsername())).thenReturn(Optional.of(this.testUser));

        this.mockUserService.updateLastLoginTime(this.testUser.getUsername());

        Assertions.assertNotNull(this.testUser.getUserStatistics().getLastLoginAt());
        Assertions.assertTrue(this.testUser.getUserStatistics().getLastLoginAt().isAfter(LocalDateTime.now().minusMinutes(1)));
        verify(this.mockUserRepository, times(1)).saveAndFlush(this.testUser);
    }
}