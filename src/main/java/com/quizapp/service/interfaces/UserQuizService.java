package com.quizapp.service.interfaces;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface UserQuizService {

    QuizDTO getSolvedQuizById(Long id);

    @Transactional
    Page<QuizDTO> getSolvedQuizzesByUsername(String username, int page, int size);

    @Transactional
    Long evaluateQuiz(QuizSubmissionDTO quizSubmissionDTO, String username);

    QuizResultDTO getQuizResult(Long id);

    boolean deleteQuizById(Long id);
}