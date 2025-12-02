package com.quizapp.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionStatsDTO {

    private Long questionId;

    private String questionText;

    private Long categoryId;

    private String categoryName;

    private int attempts;

    private int correctAnswers;

    private int wrongAnswers;

    private double accuracy;

    private double difficulty;

    private double completionRate;
}