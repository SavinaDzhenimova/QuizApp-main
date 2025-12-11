package com.quizapp.repository;

import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SolvedQuizRepositoryTest {

    @Autowired
    private SolvedQuizRepository solvedQuizRepository;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder()
                .name(RoleName.USER)
                .description("User role")
                .build();
        this.entityManager.persist(userRole);

        User user = User.builder()
                .username("user1")
                .email("user@gmail.com")
                .password("Password123")
                .roles(Set.of(userRole))
                .build();
        this.entityManager.persist(user);

        SolvedQuiz solvedQuiz1 = SolvedQuiz.builder()
                .categoryId(5L)
                .user(user)
                .solvedAt(LocalDateTime.now().minusDays(1))
                .build();
        this.solvedQuizRepository.save(solvedQuiz1);

        SolvedQuiz solvedQuiz2 = SolvedQuiz.builder()
                .categoryId(1L)
                .user(user)
                .solvedAt(LocalDateTime.now())
                .build();
        this.solvedQuizRepository.save(solvedQuiz2);
    }

    @Test
    void findByUserUsernameOrderBySolvedAtDesc_ShouldReturnEmptyPage_WhenUserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SolvedQuiz> page = this.solvedQuizRepository.findByUserUsernameOrderBySolvedAtDesc("missing", pageable);

        assertThat(page).isEmpty();
    }

    @Test
    void findByUserUsernameOrderBySolvedAtDesc_ShouldReturnPage_WhenUserFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SolvedQuiz> page = this.solvedQuizRepository.findByUserUsernameOrderBySolvedAtDesc("user1", pageable);

        assertThat(page).isNotEmpty();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getSolvedAt()).isAfter(page.getContent().get(1).getSolvedAt());
    }
}