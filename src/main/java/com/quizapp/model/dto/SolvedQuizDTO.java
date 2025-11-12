package com.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolvedQuizDTO {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private List<QuestionDTO> questions;

    private LocalDateTime solvedAt;

    private int score;

    private int maxScore;

    private Map<Long, String> userAnswers;
}