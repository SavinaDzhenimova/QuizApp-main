package com.quizapp.model.entity;

import com.quizapp.model.dto.question.QuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    private Long id;

    private String viewToken;

    private Long categoryId;

    private String categoryName;

    private List<QuestionDTO> questions;

    private LocalDateTime expireAt;
}