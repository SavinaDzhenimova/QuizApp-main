package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.repository.SolvedQuizRepository;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.SolvedQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolvedQuizServiceImpl implements SolvedQuizService {

    private final SolvedQuizRepository solvedQuizRepository;
    private final CategoryService categoryService;
    private final QuestionService questionService;

    @Override
    public SolvedQuizDTO getSolvedQuizById(Long id) {
        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(id);

        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }

        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

        List<QuestionDTO> questionDTOs = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::getQuestionById)
                .toList();

        return SolvedQuizDTO.builder()
                .id(solvedQuiz.getId())
                .categoryName(this.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                .score(solvedQuiz.getScore())
                .maxScore(solvedQuiz.getMaxScore())
                .solvedAt(solvedQuiz.getSolvedAt())
                .questions(questionDTOs)
                .build();
    }
}