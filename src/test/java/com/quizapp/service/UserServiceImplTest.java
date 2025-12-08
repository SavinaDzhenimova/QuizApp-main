package com.quizapp.service;

import com.quizapp.exception.UserNotFoundException;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.events.ForgotPasswordEvent;
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
    void updatePassword_ShouldReturnError_WhenUserNotFound() {
        
    }
}