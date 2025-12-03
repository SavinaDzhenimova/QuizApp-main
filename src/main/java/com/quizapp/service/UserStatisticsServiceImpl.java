package com.quizapp.service;

import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.UserSortField;
import com.quizapp.repository.UserStatisticsRepository;
import com.quizapp.repository.spec.UserStatisticsSpecifications;
import com.quizapp.service.interfaces.UserStatisticsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;

    @Override
    @Transactional
    public Page<UserStatisticsDTO> getUserStatisticsFiltered(String username, UserSortField sortBy, Pageable pageable) {
        Specification<UserStatistics> spec = Specification
                .allOf(UserStatisticsSpecifications.hasUsername(username))
                .and(UserStatisticsSpecifications.onlyRegularUsers());

        if (sortBy == UserSortField.LAST_SOLVED_AT) {
            spec = spec.and(UserStatisticsSpecifications.sortByLastSolvedAtNullLast());
        }

        return this.userStatisticsRepository.findAll(spec, pageable)
                .map(this::mapStatisticsToDTO);
    }

    private UserStatisticsDTO mapStatisticsToDTO(UserStatistics userStatistics) {
        return UserStatisticsDTO.builder()
                .userId(userStatistics.getUser().getId())
                .username(userStatistics.getUser().getUsername())
                .totalQuizzes(userStatistics.getTotalQuizzes())
                .totalCorrectAnswers(userStatistics.getTotalCorrectAnswers())
                .maxScore(userStatistics.getMaxScore())
                .averageScore(userStatistics.getAverageScore())
                .lastSolvedAt(userStatistics.getLastSolvedAt())
                .build();
    }

    @Override
    public UserStatistics createInitialStatistics(User user) {
        UserStatistics userStatistics = UserStatistics.builder()
                .user(user)
                .totalQuizzes(user.getSolvedQuizzes().size())
                .totalCorrectAnswers(0)
                .maxScore(0)
                .averageScore(0)
                .build();

        return this.userStatisticsRepository.saveAndFlush(userStatistics);
    }

    @Override
    public UserStatistics updateUserStatistics(UserStatistics userStatistics, long correctAnswers,
                                               int totalQuestions, LocalDateTime solvedAt) {
        userStatistics.setTotalQuizzes(userStatistics.getTotalQuizzes() + 1);
        userStatistics.setTotalCorrectAnswers(userStatistics.getTotalCorrectAnswers() + (int) correctAnswers);
        userStatistics.setMaxScore(userStatistics.getMaxScore() + totalQuestions);
        userStatistics.setAverageScore((double) userStatistics.getTotalCorrectAnswers() / userStatistics.getMaxScore() * 100);
        userStatistics.setLastSolvedAt(solvedAt);

        return this.userStatisticsRepository.saveAndFlush(userStatistics);
    }

    @Override
    public List<User> findInactiveUsers(LocalDateTime dateTime) {
        return this.userStatisticsRepository.findUsersInactiveSince(dateTime);
    }
}