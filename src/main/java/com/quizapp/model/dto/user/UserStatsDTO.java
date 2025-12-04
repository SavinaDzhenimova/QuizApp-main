package com.quizapp.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDTO {

    private int totalQuizzes;

    private int score;

    private int maxScore;

    private double averageScore;

    private LocalDateTime lastSolvedAt;
}