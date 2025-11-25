package com.quizapp.model.dto.quiz;

import com.quizapp.model.dto.question.QuestionDTO;
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
public class QuizDTO extends QuizResultDTO {

    private Long categoryId;

    private String categoryName;

    private List<QuestionDTO> questions;

    private LocalDateTime solvedAt;

    private LocalDateTime expireAt;

    private Map<Long, String> userAnswers;
}