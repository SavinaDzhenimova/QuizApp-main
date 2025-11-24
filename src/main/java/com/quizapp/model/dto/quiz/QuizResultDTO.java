package com.quizapp.model.dto.quiz;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class QuizResultDTO extends BaseQuizDTO {

    private int correctAnswers;

    private int totalQuestions;

    private double scorePercent;
}