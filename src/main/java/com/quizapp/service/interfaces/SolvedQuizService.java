package com.quizapp.service.interfaces;

import com.quizapp.model.dto.SolvedQuizDTO;

public interface SolvedQuizService {
    SolvedQuizDTO getSolvedQuizById(Long id);
}
