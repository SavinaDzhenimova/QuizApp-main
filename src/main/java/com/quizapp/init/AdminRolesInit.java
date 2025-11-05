package com.quizapp.init;

import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.RoleRepository;
import com.quizapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminRolesInit implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {

        if (this.roleRepository.count() == 0) {

            Arrays.stream(RoleName.values())
                    .forEach(roleName -> {
                        String description = switch (roleName) {
                            case ADMIN -> "Admin can add new quiz questions and categories.";
                            case USER -> "User can play quizzes and get info for his solved quizzes.";
                        };

                        Role role = Role.builder()
                                .name(roleName)
                                .description(description)
                                .build();

                        this.roleRepository.saveAndFlush(role);
                    });

            log.info("Roles initialized successfully.");
        } else {
            log.info("Roles already exist, skipping initialization.");
        }
    }

    private void initAdminUser() {

        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Admin user already exists, skipping initialization.");
            return;
        }

        Optional<Role> optionalRole = this.roleRepository.findByName(RoleName.ADMIN);

        if (optionalRole.isEmpty()) {
            log.error("ADMIN role not found, cannot create admin user!");
            return;
        }

        User adminUser = User.builder()
                .username("admin")
                .password(this.passwordEncoder.encode("Admin1234"))
                .email("admin@gmail.com")
                .role(optionalRole.get())
                .solvedQuizzes(new ArrayList<>())
                .build();

        this.userRepository.saveAndFlush(adminUser);
        log.info("Admin user created successfully");
    }
}