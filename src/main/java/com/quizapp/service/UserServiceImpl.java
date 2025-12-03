package com.quizapp.service;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.*;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.events.InactiveUserEvent;
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
            return null;
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

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .solvedQuizzes(solvedQuizDTOs)
                .totalQuizzes(user.getUserStatistics().getTotalQuizzes())
                .score(user.getUserStatistics().getTotalCorrectAnswers())
                .maxScore(user.getUserStatistics().getMaxScore())
                .averageScore(user.getUserStatistics().getAverageScore())
                .lastSolvedAt(user.getUserStatistics().getLastSolvedAt())
                .build();
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
    public Integer sendInactiveUsersEmails() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<User> inactiveUsers = this.userStatisticsService.findInactiveUsers(oneMonthAgo);

        inactiveUsers.forEach(user -> this.applicationEventPublisher.publishEvent(
                new InactiveUserEvent(this, user.getUsername(), user.getEmail())
        ));

        return inactiveUsers.size();
    }

    @Override
    public Integer removeInactiveUsersProfiles() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        List<User> inactiveUsers = this.userStatisticsService.findInactiveUsers(oneYearAgo);

        if (inactiveUsers.isEmpty()) {
            return 0;
        }

        inactiveUsers.forEach(user -> this.deleteById(user.getId()));

        return inactiveUsers.size();
    }

    @Override
    public void resetUserPassword(User user, String password) {
        user.setPassword(this.passwordEncoder.encode(password));
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
        this.userRepository.deleteById(id);
    }
}