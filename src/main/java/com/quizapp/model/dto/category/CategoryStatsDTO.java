package com.quizapp.model.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatsDTO {

    private Long categoryId;

    private String categoryName;

    private int totalStartedQuizzes;

    private int totalCompletedQuizzes;

    private int totalUnfinishedQuizzes;

    private int totalCorrectAnswers;

    private int totalQuestions;

    private double averageAccuracy;

    private double completionRate;
}