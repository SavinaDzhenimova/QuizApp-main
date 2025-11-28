package com.quizapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false, unique = true)
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    private int totalStartedQuizzes;

    private int totalCompletedQuizzes;

    private double averageScore;

    private double averageAccuracy;

    private double completionRate;
}