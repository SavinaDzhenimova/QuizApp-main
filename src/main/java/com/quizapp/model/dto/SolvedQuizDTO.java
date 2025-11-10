package com.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolvedQuizDTO {

    private Long id;

    private Long categoryId;

    private List<QuestionDTO> questions;
}