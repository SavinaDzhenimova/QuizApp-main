package com.quizapp.service;

import com.quizapp.exception.*;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.SolvedQuizRepository;
import com.quizapp.service.interfaces.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserQuizServiceImpl extends AbstractQuizService implements UserQuizService {

    private final SolvedQuizRepository solvedQuizRepository;
    private final UserService userService;
    private final UserStatisticsService userStatisticsService;

    public UserQuizServiceImpl(QuestionService questionService, CategoryService categoryService, UserService userService,
                               SolvedQuizRepository solvedQuizRepository, UserStatisticsService userStatisticsService) {
        super(questionService, categoryService);
        this.userService = userService;
        this.solvedQuizRepository = solvedQuizRepository;
        this.userStatisticsService = userStatisticsService;
    }

    @Override
    public Quiz getQuizFromTemp(String viewToken) {
        return super.getQuizFromTemp(viewToken);
    }

    @Override
    public Quiz createQuiz(Long categoryId, int numberOfQuestions) {
        return super.createQuiz(categoryId, numberOfQuestions);
    }

    @Override
    public QuizDTO getSolvedQuizById(Long id) {
        SolvedQuiz solvedQuiz = this.solvedQuizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("Куизът не е намерен."));

        List<QuestionDTO> questionDTOs = solvedQuiz.getQuestionIds().stream()
                .map(super.questionService::getQuestionById)
                .toList();

        return QuizDTO.builder()
                .id(solvedQuiz.getId())
                .categoryId(solvedQuiz.getCategoryId())
                .categoryName(super.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                .correctAnswers(solvedQuiz.getScore())
                .totalQuestions(solvedQuiz.getMaxScore())
                .solvedAt(solvedQuiz.getSolvedAt())
                .questions(questionDTOs)
                .userAnswers(solvedQuiz.getUserAnswers())
                .build();
    }

    @Override
    @Transactional
    public Page<QuizDTO> getSolvedQuizzesByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("solvedAt").descending());

        Page<SolvedQuiz> solvedQuizzesPage = this.solvedQuizRepository
                .findByUserUsernameOrderBySolvedAtDesc(username, pageable);

        return solvedQuizzesPage.map(solvedQuiz ->
                QuizDTO.builder()
                        .id(solvedQuiz.getId())
                        .categoryId(solvedQuiz.getCategoryId())
                        .categoryName(super.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                        .correctAnswers(solvedQuiz.getScore())
                        .totalQuestions(solvedQuiz.getMaxScore())
                        .solvedAt(solvedQuiz.getSolvedAt())
                        .build()
        );
    }

    @Override
    @Transactional
    public Long evaluateQuiz(String viewToken, Map<String, String> formData, String username) {
        Quiz quiz = super.loadAndRemoveTempQuiz(viewToken);

        User user = this.userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Потребителят не е намерен."));

        Map<Long, String> userAnswers = super.mapUserAnswers(formData);

        return this.saveSolvedQuiz(quiz, user, userAnswers);
    }

    private Long saveSolvedQuiz(Quiz quiz, User user, Map<Long, String> userAnswers) {
        long correctAnswers = this.getCorrectAnswers(quiz, userAnswers);
        int totalQuestions = quiz.getQuestions().size();
        LocalDateTime solvedAt = LocalDateTime.now();

        List<Long> questionIds = quiz.getQuestions().stream()
                .map(QuestionDTO::getId)
                .toList();

        SolvedQuiz solvedQuiz = SolvedQuiz.builder()
                .categoryId(quiz.getCategoryId())
                .user(user)
                .score((int) correctAnswers)
                .maxScore(totalQuestions)
                .questionIds(questionIds)
                .userAnswers(userAnswers)
                .solvedAt(solvedAt)
                .build();

        SolvedQuiz savedQuiz = this.solvedQuizRepository.saveAndFlush(solvedQuiz);

        UserStatistics userStatistics = this.userStatisticsService
                .updateUserStatistics(user.getUserStatistics(), correctAnswers, totalQuestions, solvedAt);

        user.setUserStatistics(userStatistics);
        this.userService.saveAndFlushUser(user);

        return savedQuiz.getId();
    }

    @Override
    public QuizResultDTO getQuizResult(Long id) {
        SolvedQuiz solvedQuiz = this.solvedQuizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("Куизът не е намерен."));

        int correctAnswers = solvedQuiz.getScore();
        int totalQuestions = solvedQuiz.getMaxScore();
        double scorePercent = ((double) correctAnswers / totalQuestions) * 100;

        return QuizDTO.builder()
                .id(solvedQuiz.getId())
                .totalQuestions(correctAnswers)
                .correctAnswers(totalQuestions)
                .scorePercent(scorePercent)
                .build();
    }

    @Override
    public boolean deleteQuizById(Long id) {
        if (!this.solvedQuizRepository.existsById(id)) {
            return false;
        }

        this.solvedQuizRepository.deleteById(id);
        return true;
    }
}