package com.quizapp.service.interfaces;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface UserQuizService {

    Quiz createQuiz(Long categoryId, int numberOfQuestions);

    Quiz getQuizFromTemp(String viewToken);

    QuizDTO getSolvedQuizById(Long id);

    @Transactional
    Page<QuizDTO> getSolvedQuizzesByUsername(String username, int page, int size);

    @Transactional
    Long evaluateQuiz(String viewToken, Map<String, String> formData, String username);

    QuizResultDTO getQuizResult(Long id);

    boolean deleteQuizById(Long id);
}