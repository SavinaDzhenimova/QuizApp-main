package com.quizapp.service;

import com.quizapp.exception.UserNotFoundException;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.user.UpdatePasswordDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.dto.user.UserStatsDTO;
import com.quizapp.model.entity.*;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.events.DeletionWarningEvent;
import com.quizapp.service.events.InactiveSolvingQuizzesEvent;
import com.quizapp.service.events.UserRegisterEvent;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.RoleService;
import com.quizapp.service.interfaces.UserService;
import com.quizapp.service.interfaces.UserStatisticsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserStatisticsService userStatisticsService;

    @Override
    @Transactional
    public UserDTO getUserInfo(String username) {
        Optional<User> optionalUser = this.userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("Не е намерен потребител");
        }

        User user = optionalUser.get();

        List<QuizDTO> solvedQuizDTOs = user.getSolvedQuizzes().stream()
                .sorted(Comparator.comparing(SolvedQuiz::getSolvedAt).reversed())
                .limit(3)
                .map(solvedQuiz -> QuizDTO.builder()
                        .id(solvedQuiz.getId())
                        .categoryId(solvedQuiz.getCategoryId())
                        .categoryName(this.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                        .correctAnswers(solvedQuiz.getScore())
                        .totalQuestions(solvedQuiz.getMaxScore())
                        .solvedAt(solvedQuiz.getSolvedAt())
                        .build())
                .collect(Collectors.toList());

        boolean hasRoleUser = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleName.USER));

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .solvedQuizzes(solvedQuizDTOs)
                .build();

        if (hasRoleUser) {
            UserStatsDTO userStatsDTO = UserStatsDTO.builder()
                    .totalQuizzes(user.getUserStatistics().getTotalQuizzes())
                    .score(user.getUserStatistics().getTotalCorrectAnswers())
                    .maxScore(user.getUserStatistics().getMaxScore())
                    .averageScore(user.getUserStatistics().getAverageScore())
                    .lastSolvedAt(user.getUserStatistics().getLastSolvedAt())
                    .build();

            userDTO.setUserStats(userStatsDTO);
        }

        return userDTO;
    }

    @Override
    public Result registerUser(UserRegisterDTO registerUserDTO) {
        if (registerUserDTO == null) {
            return new Result(false, "Не е намерен потребител!");
        }

        if (!registerUserDTO.getPassword().equals(registerUserDTO.getConfirmPassword())) {
            return new Result(false, "Паролите не съвпадат!");
        }

        if (this.userRepository.existsByUsername(registerUserDTO.getUsername())) {
            return new Result(false,"Вече съществува потребител с това потребителско име!");
        }

        if (this.userRepository.existsByEmail(registerUserDTO.getEmail())) {
            return new Result(false, "Вече съществува потребител с този имейл!");
        }

        Optional<Role> optionalRole = this.roleService.getRoleByName(RoleName.USER);
        if (optionalRole.isEmpty()) {
            return new Result(false, "Ролята User не съществува!");
        }

        User user = User.builder()
                .username(registerUserDTO.getUsername())
                .email(registerUserDTO.getEmail())
                .password(this.passwordEncoder.encode(registerUserDTO.getPassword()))
                .roles(Set.of(optionalRole.get()))
                .solvedQuizzes(new ArrayList<>())
                .build();

        this.userRepository.saveAndFlush(user);

        UserStatistics userStatistics = this.userStatisticsService.createInitialStatistics(user);

        user.setUserStatistics(userStatistics);

        this.userRepository.saveAndFlush(user);

        this.applicationEventPublisher.publishEvent(
                new UserRegisterEvent(this, user.getUsername(), user.getEmail()));

        return new Result(true, "Успешна регистрация!");
    }

    @Override
    public Integer sendInactiveSolvingQuizzesUsersEmails() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<UserStatistics> inactiveUsers = this.userStatisticsService.findInactiveSolvingQuizzesUsersNotWarned(oneMonthAgo);

        inactiveUsers.forEach(userStatistics -> {
            this.applicationEventPublisher.publishEvent(
                    new InactiveSolvingQuizzesEvent(this, userStatistics.getUser().getUsername(),
                            userStatistics.getUser().getEmail()));

            userStatistics.setLastSolvingWarningSent(true);
            userStatistics.setLastSolvingWarningSentAt(LocalDateTime.now());

            this.userStatisticsService.saveAndFlushUserStatistics(userStatistics);
        });

        return inactiveUsers.size();
    }

    @Override
    @Transactional
    public Integer resendWarnedInactiveSolvingQuizzesUsersEmails() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<UserStatistics> warnedInactiveUsers = this.userStatisticsService.findWarnedUsersToResendSolvingWarning(oneWeekAgo);

        warnedInactiveUsers.forEach(userStatistics -> {
            this.applicationEventPublisher.publishEvent(
                    new InactiveSolvingQuizzesEvent(this,userStatistics.getUser().getUsername(),
                            userStatistics.getUser().getEmail()));

            userStatistics.setLastSolvingWarningSentAt(LocalDateTime.now());

            this.userStatisticsService.saveAndFlushUserStatistics(userStatistics);
        });

        return warnedInactiveUsers.size();
    }

    @Override
    @Transactional
    public Integer sendInactiveUsersWarnEmail() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        List<UserStatistics> inactiveUsers = this.userStatisticsService.findInactiveNotWarned(oneYearAgo);

        inactiveUsers.forEach(userStatistics -> {
            this.applicationEventPublisher.publishEvent(
                    new DeletionWarningEvent(this, userStatistics.getUser().getUsername(),
                            userStatistics.getUser().getEmail()));

            userStatistics.setDeletionWarningSent(true);
            userStatistics.setDeletionWarningSentAt(LocalDateTime.now());

            this.userStatisticsService.saveAndFlushUserStatistics(userStatistics);
        });

        return inactiveUsers.size();
    }

    @Override
    public Integer removeWarnedInactiveLoginUsersAccounts() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusWeeks(1);

        List<User> inactiveUsers = this.userStatisticsService.findInactiveLoginUsersWarned(oneYearAgo);

        if (inactiveUsers.isEmpty()) {
            return 0;
        }

        inactiveUsers.forEach(user -> this.deleteById(user.getId()));

        return inactiveUsers.size();
    }

    @Override
    public Result updatePassword(String username, UpdatePasswordDTO updatePasswordDTO) {
        if (updatePasswordDTO == null) {
            return new Result(false, "Невалидни входни данни!");
        }

        Optional<User> optionalUser = this.userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return new Result(false, "Потребителят не е намерен!");
        }

        User user = optionalUser.get();

        if (!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new Result(false, "Паролите не съвпадат!");
        }

        if (!this.passwordEncoder.matches(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            return new Result(false, "Старата парола е грешна!");
        }

        if (this.passwordEncoder.matches(updatePasswordDTO.getNewPassword(), user.getPassword())) {
            return new Result(false, "Новата парола не може да е като старата парола!");
        }

        user.setPassword(this.passwordEncoder.encode(updatePasswordDTO.getNewPassword()));

        this.userRepository.saveAndFlush(user);

        return new Result(true, "Успешно променихте паролата си.");
    }

    @Override
    public void resetUserPassword(User user, String password) {
        user.setPassword(this.passwordEncoder.encode(password));
        this.userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    public void updateLastLoginTime(String username) {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Не е намерен потребител " + username + "."));

        if (user.getUserStatistics() != null) {
            user.getUserStatistics().setLastLoginAt(LocalDateTime.now());
        }

        this.userRepository.saveAndFlush(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public User saveAndFlushUser(User user) {
        return this.userRepository.saveAndFlush(user);
    }

    @Override
    public void deleteById(Long id) {

        Optional<User> optionalUser = this.userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        user.getRoles().clear();
        this.userRepository.saveAndFlush(user);

        this.userRepository.deleteById(id);
    }
}