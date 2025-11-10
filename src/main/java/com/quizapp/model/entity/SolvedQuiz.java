package com.quizapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solved_quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolvedQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "solved_quiz_questions", joinColumns = @JoinColumn(name = "solved_quiz_id"))
    @Column(name = "question_id")
    private List<Long> questionIds = new ArrayList<>();

    @Column
    private int score;

    @Column(name = "max_score")
    private int maxScore;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;
}