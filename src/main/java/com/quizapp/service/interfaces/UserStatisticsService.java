package com.quizapp.service.interfaces;

import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserStatisticsService {

    Page<UserStatisticsDTO> getUserStatisticsFiltered(Pageable pageable);

    UserStatistics createInitialStatistics(User user);

    UserStatistics updateUserStatistics(UserStatistics userStatistics, long correctAnswers, int totalQuestions, LocalDateTime solvedAt);

    List<User> findInactiveUsers(LocalDateTime dateTime);
}