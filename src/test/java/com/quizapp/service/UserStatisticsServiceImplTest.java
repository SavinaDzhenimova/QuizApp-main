package com.quizapp.service;

import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.UserSortField;
import com.quizapp.repository.UserStatisticsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserStatisticsServiceImplTest {

    @Mock
    private UserStatisticsRepository mockUserStatsRepository;
    @InjectMocks
    private UserStatisticsServiceImpl mockUserStatsService;

    private UserStatistics mockUserStats;
    private User mockUser;

    @BeforeEach
    void setUp() {
        this.mockUser = User.builder()
                .id(1L)
                .username("user1")
                .email("user@gmail.com")
                .solvedQuizzes(new ArrayList<>())
                .build();

        this.mockUserStats = UserStatistics.builder()
                .id(1L)
                .user(this.mockUser)
                .totalQuizzes(5)
                .totalCorrectAnswers(20)
                .maxScore(25)
                .averageScore(80.00)
                .build();
    }

    @Test
    void getUserStatisticsFiltered_ShouldReturnPageUserStatsDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserStatistics> page = new PageImpl<>(List.of(this.mockUserStats));

        ArgumentCaptor<Specification<UserStatistics>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);

        when(this.mockUserStatsRepository.findAll(ArgumentMatchers.<Specification<UserStatistics>>any(), eq(pageable))).thenReturn(page);

        Page<UserStatisticsDTO> result = this.mockUserStatsService.getUserStatisticsFiltered("user1", null, pageable);

        verify(this.mockUserStatsRepository, times(1))
                .findAll(specCaptor.capture(), eq(pageable));

        Assertions.assertNotNull(specCaptor.getValue());
        Assertions.assertEquals(1, result.getTotalElements());

        UserStatisticsDTO dto = result.getContent().get(0);

        Assertions.assertEquals(this.mockUserStats.getUser().getId(), dto.getUserId());
        Assertions.assertEquals(this.mockUserStats.getTotalQuizzes(), dto.getTotalQuizzes());
        Assertions.assertEquals(this.mockUserStats.getTotalCorrectAnswers(), dto.getTotalCorrectAnswers());
        Assertions.assertEquals(this.mockUserStats.getMaxScore(), dto.getMaxScore());
        Assertions.assertEquals(this.mockUserStats.getAverageScore(), dto.getAverageScore());
    }

    @Test
    void getUserStatisticsFiltered_ShouldAddSortSpecification_WhenSortByLastSolvedAt() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserStatistics> page = new PageImpl<>(List.of(this.mockUserStats));

        ArgumentCaptor<Specification<UserStatistics>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);

        when(this.mockUserStatsRepository
                .findAll(ArgumentMatchers.<Specification<UserStatistics>>any(), eq(pageable))).thenReturn(page);

        Page<UserStatisticsDTO> result = this.mockUserStatsService.getUserStatisticsFiltered("user1", UserSortField.LAST_SOLVED_AT, pageable);

        verify(this.mockUserStatsRepository).findAll(specCaptor.capture(), eq(pageable));

        Assertions.assertNotNull(specCaptor.getValue());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserStatisticsFiltered_ShouldReturnEmptyPage_WhenNoData() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserStatistics> emptyPage = new PageImpl<>(List.of());

        when(this.mockUserStatsRepository.findAll(ArgumentMatchers.<Specification<UserStatistics>>any(), eq(pageable)))
                .thenReturn(emptyPage);

        Page<UserStatisticsDTO> result = this.mockUserStatsService.getUserStatisticsFiltered("nonexistent", null, pageable);

        verify(this.mockUserStatsRepository, times(1))
                .findAll(ArgumentMatchers.<Specification<UserStatistics>>any(), eq(pageable));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getContent().isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
    }

    @Test
    void createInitialStatistics_ShouldCreateNewUserStatsForUser() {
        ArgumentCaptor<UserStatistics> captor = ArgumentCaptor.forClass(UserStatistics.class);

        when(this.mockUserStatsRepository.saveAndFlush(any(UserStatistics.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserStatistics result = this.mockUserStatsService.createInitialStatistics(this.mockUser);

        verify(this.mockUserStatsRepository, times(1)).saveAndFlush(captor.capture());
        UserStatistics saved = captor.getValue();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.mockUser, saved.getUser());
        Assertions.assertEquals(0, saved.getTotalQuizzes());
        Assertions.assertEquals(0, saved.getTotalCorrectAnswers());
        Assertions.assertEquals(0, saved.getMaxScore());
        Assertions.assertEquals(0, saved.getAverageScore());
        Assertions.assertFalse(saved.isDeletionWarningSent());
        Assertions.assertNull(saved.getDeletionWarningSentAt());
        Assertions.assertFalse(saved.isLastSolvingWarningSent());
        Assertions.assertNull(saved.getLastSolvingWarningSentAt());
    }

    @Test
    void updateUserStatistics_ShouldChangeUserStats() {
        when(this.mockUserStatsRepository.saveAndFlush(this.mockUserStats)).thenReturn(this.mockUserStats);

        UserStatistics updatedUserStats = this.mockUserStatsService.updateUserStatistics(this.mockUserStats, 3, 5, LocalDateTime.now().plusMinutes(30));

        Assertions.assertEquals(6, updatedUserStats.getTotalQuizzes());
        Assertions.assertEquals(23, updatedUserStats.getTotalCorrectAnswers());
        Assertions.assertEquals(30, updatedUserStats.getMaxScore());

        double avgScore = (double) updatedUserStats.getTotalCorrectAnswers() / updatedUserStats.getMaxScore() * 100.00;
        Assertions.assertEquals(avgScore, updatedUserStats.getAverageScore());

        Assertions.assertNotNull(updatedUserStats.getLastSolvedAt());
        verify(this.mockUserStatsRepository, times(1)).saveAndFlush(this.mockUserStats);
    }

    @Test
    void findInactiveSolvingQuizzesUsersNotWarned_ShouldReturnZero_WhenUserStatsNotFound() {
        when(this.mockUserStatsRepository.findInactiveSolvingQuizzesUsersNotWarned(any()))
                .thenReturn(Collections.emptyList());

        List<UserStatistics> result = this.mockUserStatsService.findInactiveSolvingQuizzesUsersNotWarned(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void findInactiveSolvingQuizzesUsersNotWarned_ShouldReturnListUserStats_WhenUsersFound() {
        when(this.mockUserStatsRepository.findInactiveSolvingQuizzesUsersNotWarned(any()))
                .thenReturn(List.of(this.mockUserStats));

        List<UserStatistics> result = this.mockUserStatsService.findInactiveSolvingQuizzesUsersNotWarned(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(this.mockUser, result.get(0).getUser());
    }

    @Test
    void findWarnedUsersToResendSolvingWarning_ShouldReturnZero_WhenUserStatsNotFound() {
        when(this.mockUserStatsRepository.findWarnedUsersToResendSolvingWarning(any()))
                .thenReturn(Collections.emptyList());

        List<UserStatistics> result = this.mockUserStatsService.findWarnedUsersToResendSolvingWarning(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void findWarnedUsersToResendSolvingWarning_ShouldReturnListUserStats_WhenUsersFound() {
        when(this.mockUserStatsRepository.findWarnedUsersToResendSolvingWarning(any()))
                .thenReturn(List.of(this.mockUserStats));

        List<UserStatistics> result = this.mockUserStatsService.findWarnedUsersToResendSolvingWarning(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(this.mockUser, result.get(0).getUser());
    }

    @Test
    void findInactiveLoginUsersWarned_ShouldReturnZero_WhenUserStatsNotFound() {
        when(this.mockUserStatsRepository.findInactiveLoginUsersWarned(any()))
                .thenReturn(Collections.emptyList());

        List<User> result = this.mockUserStatsService.findInactiveLoginUsersWarned(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void findInactiveLoginUsersWarned_ShouldReturnListUserStats_WhenUsersFound() {
        when(this.mockUserStatsRepository.findInactiveLoginUsersWarned(any()))
                .thenReturn(List.of(this.mockUser));

        List<User> result = this.mockUserStatsService.findInactiveLoginUsersWarned(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(this.mockUser, result.get(0));
    }

    @Test
    void findInactiveNotWarned_ShouldReturnZero_WhenUserStatsNotFound() {
        when(this.mockUserStatsRepository.findInactiveNotWarned(any()))
                .thenReturn(Collections.emptyList());

        List<UserStatistics> result = this.mockUserStatsService.findInactiveNotWarned(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void findInactiveNotWarned_ShouldReturnListUserStats_WhenUsersFound() {
        when(this.mockUserStatsRepository.findInactiveNotWarned(any()))
                .thenReturn(List.of(this.mockUserStats));

        List<UserStatistics> result = this.mockUserStatsService.findInactiveNotWarned(any());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(this.mockUser, result.get(0).getUser());
    }

    @Test
    void saveAndFlushUserStatistics_ShouldSaveUserStats() {
        this.mockUserStatsService.saveAndFlushUserStatistics(this.mockUserStats);

        verify(this.mockUserStatsRepository, times(1)).saveAndFlush(this.mockUserStats);
    }
}