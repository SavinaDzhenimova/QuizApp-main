package com.quizapp.repository;

import com.quizapp.model.entity.PasswordResetToken;
import com.quizapp.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepo;

    private PasswordResetToken passwordResetToken;
    private PasswordResetToken usedToken;
    private PasswordResetToken expiredToken;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("user1")
                .email("user@gmail.com")
                .password("Password123")
                .build();
        this.entityManager.persist(user);

        this.passwordResetToken = PasswordResetToken.builder()
                .token("token123")
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .used(false)
                .build();
        this.passwordResetTokenRepo.save(this.passwordResetToken);

        this.usedToken = PasswordResetToken.builder()
                .token("used")
                .used(true)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        this.passwordResetTokenRepo.save(this.usedToken);

        this.expiredToken = PasswordResetToken.builder()
                .token("expired")
                .used(false)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        this.passwordResetTokenRepo.save(this.expiredToken);
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
        assertThat(optionalPasswordResetToken.get().getUser()).isNotNull();
        assertThat(optionalPasswordResetToken.get().getToken()).isEqualTo("token123");
    }

    @Test
    void findByUserId_ShouldReturnEmpty_WhenUserNotFound() {
        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo.findByUserId(1L);

        assertThat(optionalPasswordResetToken).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnPasswordResetToken_WhenUserFound() {
        Optional<PasswordResetToken> optionalPasswordResetToken = this.passwordResetTokenRepo
                .findByUserId(this.passwordResetToken.getUser().getId());

        assertThat(optionalPasswordResetToken).isPresent();
        assertThat(optionalPasswordResetToken.get().getUser()).isNotNull();
        assertThat(optionalPasswordResetToken.get().getUser().getId()).isEqualTo(this.passwordResetToken.getUser().getId());
    }

    @Test
    void deleteExpiredOrUsedTokens_ShouldDeleteToken_WhenUsedOrExpired() {
        this.passwordResetTokenRepo.deleteExpiredOrUsedTokens(LocalDateTime.now());

        List<PasswordResetToken> tokens = this.passwordResetTokenRepo.findAll();

        assertThat(tokens).doesNotContain(this.usedToken);
        assertThat(tokens).doesNotContain(this.expiredToken);
        assertThat(tokens).hasSize(1);
        assertThat(tokens).contains(this.passwordResetToken);
    }
}