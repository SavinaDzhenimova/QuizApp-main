package com.quizapp.service;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.dto.user.AdminDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.repository.spec.UserSpecifications;
import com.quizapp.service.events.AddedAdminEvent;
import com.quizapp.service.interfaces.AdminService;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Page<AdminDTO> getAllAdmins(String username, Pageable pageable) {
        Specification<User> spec = Specification
                .allOf(UserSpecifications.hasUsername(username))
                .and(UserSpecifications.onlyAdminUsers());

        return this.userRepository.findAll(spec, pageable)
                .map(user -> AdminDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build());
    }

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

        String tempPassword = UUID.randomUUID().toString();
        User admin = User.builder()
                .username(addAdminDTO.getUsername())
                .email(addAdminDTO.getEmail())
                .password(this.passwordEncoder.encode(tempPassword))
                .roles(Set.of(optionalRole.get()))
                .solvedQuizzes(new ArrayList<>())
                .build();

        this.userRepository.saveAndFlush(admin);

        this.sendAdminEmailToChangePassword(admin);

        return new Result(true, "Успешно добавихте нов админ.");
    }

    private void sendAdminEmailToChangePassword(User user) {
        String token = this.passwordResetService.createTokenForUser(user);

        this.applicationEventPublisher.publishEvent(
                new AddedAdminEvent(this, user.getUsername(), user.getEmail(), token));
    }

    @Override
    public Result deleteAdminById(Long id) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return new Result(false, "Не е намерен админ.");
        }

        User user = optionalUser.get();
        user.getRoles().clear();
        this.userRepository.saveAndFlush(user);

        this.userRepository.deleteById(id);
        return new Result(true, "Успешно премахнахте админ " + user.getUsername() + ".");
    }
}