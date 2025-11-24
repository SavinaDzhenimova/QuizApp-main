package com.quizapp.service.interfaces;

import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.SolvedQuiz;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface SolvedQuizService {

    QuizDTO getSolvedQuizById(Long id);

    @Transactional
    Page<QuizDTO> getSolvedQuizzesByUsername(String username, int page, int size);

    SolvedQuiz createQuiz(Long categoryId, int numberOfQuestions, String username);

    @Transactional
    QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> formData, String username);

    boolean deleteQuizById(Long id);
}
