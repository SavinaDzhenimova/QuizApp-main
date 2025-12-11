package com.quizapp.repository;

import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.spec.UserStatisticsSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserStatisticsRepositoryTest {

    @Autowired
    private UserStatisticsRepository userStatisticsRepo;
    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private UserStatistics stats1;
    private UserStatistics stats2;

    @BeforeEach
    void setUp() {
        Role roleUser = Role.builder().name(RoleName.USER).description("User role").build();
        Role roleAdmin = Role.builder().name(RoleName.ADMIN).description("Admin role").build();
        this.entityManager.persist(roleUser);
        this.entityManager.persist(roleAdmin);

        this.user1 = User.builder().username("john").email("john@example.com").roles(Set.of(roleUser)).password("pass").build();
        this.user2 = User.builder().username("jane").email("jane@example.com").roles(Set.of(roleAdmin)).password("pass").build();
        this.entityManager.persist(this.user1);
        this.entityManager.persist(this.user2);

        this.stats1 = UserStatistics.builder()
                .user(this.user1)
                .lastSolvedAt(LocalDateTime.now().minusDays(5))
                .lastSolvingWarningSent(false)
                .lastLoginAt(LocalDateTime.now().minusDays(10))
                .deletionWarningSent(false)
                .build();

        this.stats2 = UserStatistics.builder()
                .user(this.user2)
                .lastSolvedAt(null)
                .lastSolvingWarningSent(true)
                .lastSolvingWarningSentAt(LocalDateTime.now().minusDays(7))
                .lastLoginAt(null)
                .deletionWarningSent(true)
                .deletionWarningSentAt(LocalDateTime.now().minusDays(15))
                .build();

        this.entityManager.persist(this.stats1);
        this.entityManager.persist(this.stats2);
        this.entityManager.flush();
    }

    @Test
    void findInactiveSolvingQuizzesUsersNotWarned_ShouldReturnCorrectUsers() {
        List<UserStatistics> result = this.userStatisticsRepo
                .findInactiveSolvingQuizzesUsersNotWarned(LocalDateTime.now().minusDays(1));

        assertThat(result).isNotEmpty();
        assertThat(result).contains(this.stats1);
        assertThat(result).doesNotContain(this.stats2);
    }

    @Test
    void findWarnedUsersToResendSolvingWarning_ShouldReturnCorrectUsers() {
        List<UserStatistics> result = this.userStatisticsRepo.findWarnedUsersToResendSolvingWarning(LocalDateTime.now());

        assertThat(result).isNotEmpty();
        assertThat(result).contains(this.stats2);
        assertThat(result).doesNotContain(this.stats1);
    }

    @Test
    void findInactiveNotWarned_ShouldReturnCorrectUsers() {
        List<UserStatistics> result = this.userStatisticsRepo.findInactiveNotWarned(LocalDateTime.now());

        assertThat(result).isNotEmpty();
        assertThat(result).contains(this.stats1);
        assertThat(result).doesNotContain(this.stats2);
    }

    @Test
    void findInactiveLoginUsersWarned_ShouldReturnCorrectUsers() {
        List<User> result = this.userStatisticsRepo.findInactiveLoginUsersWarned(LocalDateTime.now());

        assertThat(result).isNotEmpty();
        assertThat(result).contains(this.user2);
        assertThat(result).doesNotContain(this.user1);
    }

    @Test
    void findAll_ShouldReturnEmptyPage_WhenUserStatsNotFound() {
        Specification<UserStatistics> spec = Specification
                .allOf(UserStatisticsSpecifications.hasUsername("missing"))
                .and(UserStatisticsSpecifications.onlyRegularUsers());
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserStatistics> page = this.userStatisticsRepo.findAll(spec, pageable);

        assertThat(page).isEmpty();
    }

    @Test
    void findAll_ShouldReturnPage_WhenUserStatsFound() {
        Specification<UserStatistics> spec = Specification
                .allOf(UserStatisticsSpecifications.hasUsername(""))
                .and(UserStatisticsSpecifications.onlyRegularUsers());
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserStatistics> page = this.userStatisticsRepo.findAll(spec, pageable);

        assertThat(page).isNotEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getUser()).isEqualTo(this.user1);
    }

    @Test
    void findAll_ShouldReturnUserStatsSortedByLastSolvedAt() {
        Specification<UserStatistics> spec = Specification
                .allOf(UserStatisticsSpecifications.sortByLastSolvedAtNullLast());
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserStatistics> page = this.userStatisticsRepo.findAll(spec, pageable);

        assertThat(page).isNotEmpty();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getLastSolvedAt()).isEqualTo(this.stats1.getLastSolvedAt());
        assertThat(page.getContent().get(1).getLastSolvedAt()).isEqualTo(this.stats2.getLastSolvedAt());
        assertThat(page.getContent().get(0).getLastSolvedAt()).isNotNull();
        assertThat(page.getContent().get(1).getLastSolvedAt()).isNull();
    }
}