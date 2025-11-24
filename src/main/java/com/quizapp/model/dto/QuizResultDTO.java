package com.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizResultDTO {

    private int correctAnswers;

    private int totalQuestions;

    private double scorePercent;

    private String token;

    private Long id;
}