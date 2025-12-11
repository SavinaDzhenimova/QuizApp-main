package com.quizapp.repository;

import com.quizapp.model.entity.Role;
import com.quizapp.model.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder()
                .name(RoleName.USER)
                .description("Role User")
                .build();
        this.roleRepository.save(userRole);
    }

    @Test
    void findByRoleName_ShouldReturnEmpty_WhenRoleNotFound() {
        Optional<Role> optionalRole = this.roleRepository.findByName(RoleName.ADMIN);

        assertThat(optionalRole).isEmpty();
    }

    @Test
    void findByRoleName_ShouldReturnRole_WhenRoleFound() {
        Optional<Role> optionalRole = this.roleRepository.findByName(RoleName.USER);

        assertThat(optionalRole).isPresent();
        assertThat(optionalRole.get().getName()).isEqualTo(RoleName.USER);
    }
}