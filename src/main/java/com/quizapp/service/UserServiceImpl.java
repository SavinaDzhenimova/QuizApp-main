package com.quizapp.service;

import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.interfaces.RoleService;
import com.quizapp.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

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
        return new Result(true, "Успешна регистрация!");
    }
}