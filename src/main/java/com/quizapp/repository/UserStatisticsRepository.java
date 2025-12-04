package com.quizapp.repository;

import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    @Query("SELECT us FROM UserStatistics us WHERE (us.lastSolvedAt < :dateTime OR us.lastSolvedAt IS NULL) AND us.lastSolvingWarningSent = false")
    List<UserStatistics> findInactiveSolvingQuizzesUsersNotWarned(LocalDateTime dateTime);

    @Query("SELECT us FROM UserStatistics us WHERE us.lastSolvingWarningSent = true AND us.lastSolvingWarningSentAt < :dateTime AND (us.lastSolvedAt IS NULL OR us.lastSolvedAt < us.lastSolvingWarningSentAt)")
    List<UserStatistics> findWarnedUsersToResendSolvingWarning(LocalDateTime dateTime);

    @Query("SELECT us FROM UserStatistics us WHERE (us.lastLoginAt < :dateTime OR us.lastLoginAt IS NULL) AND us.deletionWarningSent = false")
    List<UserStatistics> findInactiveNotWarned(LocalDateTime dateTime);

    @Query("SELECT us.user FROM UserStatistics us WHERE us.deletionWarningSent = true AND us.deletionWarningSentAt < :dateTime AND (us.lastLoginAt IS NULL OR us.lastLoginAt < us.deletionWarningSentAt)")
    List<User> findInactiveLoginUsersWarned(LocalDateTime dateTime);

    Page<UserStatistics> findAll(Specification<UserStatistics> spec, Pageable pageable);
}