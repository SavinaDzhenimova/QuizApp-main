package com.quizapp.model.dto;

import com.quizapp.model.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {

    private Long id;

    private Long categoryId;

    private List<QuestionDTO> questions;
}