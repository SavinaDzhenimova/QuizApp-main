package com.quizapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false, unique = true)
    private Long questionId;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    private int attempts;

    private int correctAnswers;

    private int wrongAnswers;

    private double accuracy;

    private double difficulty;

    private double completionRate;
}