package com.quizapp.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionApiDTO {

    private Long id;

    private String questionText;

    private Long categoryId;

    private String categoryName;

    private String correctAnswer;

    private List<String> options;
}