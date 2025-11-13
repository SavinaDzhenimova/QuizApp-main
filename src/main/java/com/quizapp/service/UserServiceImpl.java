package com.quizapp.service;

import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.*;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.RoleService;
import com.quizapp.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO getUserInfo(String username) {
        Optional<User> optionalUser = this.userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        List<SolvedQuizDTO> solvedQuizDTOs = user.getSolvedQuizzes().stream()
                .map(solvedQuiz -> SolvedQuizDTO.builder()
                        .id(solvedQuiz.getId())
                        .categoryId(solvedQuiz.getCategoryId())
                        .categoryName(this.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                        .score(solvedQuiz.getScore())
                        .maxScore(solvedQuiz.getMaxScore())
                        .solvedAt(solvedQuiz.getSolvedAt())
                        .build())
                .limit(3)
                .sorted(Comparator.comparing(SolvedQuizDTO::getSolvedAt).reversed())
                .toList();

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

        UserStatistics userStatistics = UserStatistics.builder()
                .user(user)
                .totalQuizzes(user.getSolvedQuizzes().size())
                .totalCorrectAnswers(0)
                .maxScore(0)
                .averageScore(0)
                .build();

        user.setUserStatistics(userStatistics);

        this.userRepository.saveAndFlush(user);

        return new Result(true, "Успешна регистрация!");
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    public User saveAndFlushUser(User user) {
        return this.userRepository.saveAndFlush(user);
    }
}