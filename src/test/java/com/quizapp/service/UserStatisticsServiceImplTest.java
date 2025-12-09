package com.quizapp.service;

import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.dto.user.UserStatsDTO;
import com.quizapp.model.entity.CategoryStatistics;
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

    
}