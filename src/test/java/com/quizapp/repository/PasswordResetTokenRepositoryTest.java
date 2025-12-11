package com.quizapp.repository;

import com.quizapp.model.entity.PasswordResetToken;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PasswordResetTokenRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepo;

    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
        this.passwordResetToken = PasswordResetToken.builder()
                .token("token123")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .used(false)
                .build();

        this.passwordResetTokenRepo.save(passwordResetToken);
    }

    @Test
    void findByToken_ShouldReturnEmpty_WhenTokenNotFound() {
        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByToken("missing");

        assertThat(optionalPasswordResetToken).isEmpty();
    }

    @Test
    void findByToken_ShouldReturnPasswordResetToken_WhenTokenFound() {
        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByToken("token123");

        assertThat(optionalPasswordResetToken).isPresent();
        assertThat(optionalPasswordResetToken.get().getUser()).isNull();
        assertThat(optionalPasswordResetToken.get().getToken()).isEqualTo("token123");
    }

    @Test
    void findByUserId_ShouldReturnEmpty_WhenUserNotFound() {
        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByUserId(1L);

        assertThat(optionalPasswordResetToken).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnPasswordResetToken_WhenUserFound() {
        Role userRole = new Role();
        userRole.setName(RoleName.USER);
        userRole.setDescription("User role");
        this.roleRepository.save(userRole);

        User user = User.builder()
                .username("user1")
                .email("user@gmail.com")
                .password("Password123")
                .roles(Set.of(userRole))
                .build();
        this.userRepository.save(user);

        this.passwordResetToken.setUser(user);
        this.passwordResetTokenRepo.save(this.passwordResetToken);

        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByUserId(1L);

        assertThat(optionalPasswordResetToken).isPresent();
        assertThat(optionalPasswordResetToken.get().getUser()).isNotNull();
        assertThat(optionalPasswordResetToken.get().getUser().getId()).isEqualTo(1L);
    }

    @Test
    void deleteExpiredOrUsedTokens_ShouldDeleteToken_WhenUsed() {
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("used")
                .used(true)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        this.passwordResetTokenRepo.save(usedToken);

        this.passwordResetTokenRepo.deleteExpiredOrUsedTokens(LocalDateTime.now());

        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByToken("used");
        List<PasswordResetToken> tokens = this.passwordResetTokenRepo.findAll();

        assertThat(optionalPasswordResetToken).isEmpty();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("token123");
    }

    @Test
    void deleteExpiredOrUsedTokens_ShouldDeleteToken_WhenExpired() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired")
                .used(false)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        this.passwordResetTokenRepo.save(expiredToken);

        this.passwordResetTokenRepo.deleteExpiredOrUsedTokens(LocalDateTime.now());

        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByToken("expired");
        List<PasswordResetToken> tokens = this.passwordResetTokenRepo.findAll();

        assertThat(optionalPasswordResetToken).isEmpty();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("token123");
    }
}