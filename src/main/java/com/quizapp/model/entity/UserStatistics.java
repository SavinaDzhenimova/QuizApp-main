package com.quizapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_quizzes", nullable = false)
    private int totalQuizzes;

    @Column(name = "total_correct_answers", nullable = false)
    private int totalCorrectAnswers;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(name = "average_score", nullable = false)
    private double averageScore;
}