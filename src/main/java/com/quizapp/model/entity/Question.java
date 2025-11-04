package com.quizapp.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    private Long id;

    private String questionText;

    private Category category;

    private String correctAnswer;

    private List<String> options = new ArrayList<>();
}