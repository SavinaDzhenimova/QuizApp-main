package com.quizapp.repository;

import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.spec.UserSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Role adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .description("Admin role")
                .build();
        this.entityManager.persist(adminRole);

        User user = User.builder()
                .username("user1")
                .email("user@gmail.com")
                .password("Password123")
                .roles(Set.of(adminRole))
                .build();
        this.userRepository.save(user);
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserNotFound() {
        boolean exists = this.userRepository.existsByUsername("missing");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserFound() {
        boolean exists = this.userRepository.existsByUsername("user1");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenUserNotFound() {
        boolean exists = this.userRepository.existsByEmail("missing");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenUserFound() {
        boolean exists = this.userRepository.existsByEmail("user@gmail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserNotFound() {
        Optional<User> optionalUser = this.userRepository.findByUsername("missing");

        assertThat(optionalUser).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserFound() {
        Optional<User> optionalUser = this.userRepository.findByUsername("user1");

        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getUsername()).isEqualTo("user1");
        assertThat(optionalUser.get().getEmail()).isEqualTo("user@gmail.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserNotFound() {
        Optional<User> optionalUser = this.userRepository.findByEmail("missing");

        assertThat(optionalUser).isEmpty();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserFound() {
        Optional<User> optionalUser = this.userRepository.findByEmail("user@gmail.com");

        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getUsername()).isEqualTo("user1");
        assertThat(optionalUser.get().getEmail()).isEqualTo("user@gmail.com");
    }

    @Test
    void findAll_ShouldReturnEmptyPage_WhenUsersNotFound() {
        Specification<User> spec = Specification
                .allOf(UserSpecifications.hasUsername("admin"))
                .and(UserSpecifications.onlyAdminUsers());
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = this.userRepository.findAll(spec, pageable);

        assertThat(page).isEmpty();
    }

    @Test
    void findAll_ShouldReturnPage_WhenUsersFound() {
        Specification<User> spec = Specification
                .allOf(UserSpecifications.hasUsername(""))
                .and(UserSpecifications.onlyAdminUsers());
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = this.userRepository.findAll(spec, pageable);

        assertThat(page).isNotEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getUsername()).isEqualTo("user1");
    }
}