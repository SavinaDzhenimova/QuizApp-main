package com.quizapp.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class QuizDTO extends BaseQuizDTO {

    private Long categoryId;

    private String categoryName;

    private List<QuestionDTO> questions;

    private LocalDateTime solvedAt;

    private int score;

    private int maxScore;

    private Map<Long, String> userAnswers;
}