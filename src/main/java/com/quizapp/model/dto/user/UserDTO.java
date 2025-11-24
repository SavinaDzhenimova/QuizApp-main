package com.quizapp.model.dto.user;

import com.quizapp.model.dto.QuizDTO;
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
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    private List<QuizDTO> solvedQuizzes;

    private int totalQuizzes;

    private int score;

    private int maxScore;

    private double averageScore;

    private LocalDateTime lastSolvedAt;
}