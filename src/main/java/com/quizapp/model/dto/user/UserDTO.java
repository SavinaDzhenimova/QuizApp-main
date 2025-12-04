package com.quizapp.model.dto.user;

import com.quizapp.model.dto.quiz.QuizDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    private UserStatsDTO userStats;

    private List<QuizDTO> solvedQuizzes;
}