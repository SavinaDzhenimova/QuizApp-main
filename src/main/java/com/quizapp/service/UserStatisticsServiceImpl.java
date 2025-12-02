package com.quizapp.service;

import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.UserStatisticsRepository;
import com.quizapp.service.interfaces.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;

    public Page<UserStatisticsDTO> getUserStatisticsFiltered(Pageable pageable) {
        return this.userStatisticsRepository.findAll(pageable)
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