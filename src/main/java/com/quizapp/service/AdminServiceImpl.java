package com.quizapp.service;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.events.AddedAdminEvent;
import com.quizapp.service.interfaces.AdminService;
import com.quizapp.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Result addAdmin(AddAdminDTO addAdminDTO) {
        if (addAdminDTO == null) {
            return new Result(false, "Не е намерен дмин!");
        }

        if (this.userRepository.existsByUsername(addAdminDTO.getUsername())) {
            return new Result(false,"Вече съществува потребител с това потребителско име!");
        }

        if (this.userRepository.existsByEmail(addAdminDTO.getEmail())) {
            return new Result(false, "Вече съществува потребител с този имейл!");
        }

        Optional<Role> optionalRole = this.roleService.getRoleByName(RoleName.ADMIN);
        if (optionalRole.isEmpty()) {
            return new Result(false, "Ролята Admin не съществува!");
        }

        User admin = User.builder()
                .username(addAdminDTO.getUsername())
                .email(addAdminDTO.getEmail())
                .password(this.passwordEncoder.encode(addAdminDTO.getTempPassword()))
                .roles(Set.of(optionalRole.get()))
                .solvedQuizzes(new ArrayList<>())
                .build();

        this.userRepository.saveAndFlush(admin);

        this.applicationEventPublisher.publishEvent(
                new AddedAdminEvent(this, admin.getUsername(), admin.getEmail(), addAdminDTO.getTempPassword()));

        return new Result(true, "Успешно добавихте нов админ.");
    }
}