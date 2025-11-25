package com.quizapp.model.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class QuizResultDTO {

    private Long id;

    private String viewToken;

    private int correctAnswers;

    private int totalQuestions;

    private double scorePercent;
}
