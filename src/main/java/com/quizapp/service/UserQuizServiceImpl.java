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
import com.quizapp.service.utils.AbstractQuizHelper;
import com.quizapp.service.utils.TempQuizStorage;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserQuizServiceImpl extends AbstractQuizHelper implements UserQuizService {

    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final SolvedQuizRepository solvedQuizRepository;
    private final UserService userService;
    private final UserStatisticsService userStatisticsService;
    private final CategoryStatisticsService categoryStatisticsService;

    public UserQuizServiceImpl(TempQuizStorage tempQuizStorage, QuestionService questionService,
                               CategoryService categoryService, SolvedQuizRepository solvedQuizRepository,
                               UserService userService, UserStatisticsService userStatisticsService,
                               CategoryStatisticsService categoryStatisticsService) {
        super(tempQuizStorage);
        this.questionService = questionService;
        this.categoryService = categoryService;
        this.solvedQuizRepository = solvedQuizRepository;
        this.userService = userService;
        this.userStatisticsService = userStatisticsService;
        this.categoryStatisticsService = categoryStatisticsService;
    }

    @Override
    public QuizDTO getSolvedQuizById(Long id) {
        SolvedQuiz solvedQuiz = this.solvedQuizRepository.findById(id)
                .orElseThrow(() -> new QuizNotFoundException("Куизът не е намерен."));

        List<QuestionDTO> questionDTOs = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::getQuestionById)
                .toList();

        return QuizDTO.builder()
                .id(solvedQuiz.getId())
                .categoryId(solvedQuiz.getCategoryId())
                .categoryName(this.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
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
                        .categoryName(this.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                        .correctAnswers(solvedQuiz.getScore())
                        .totalQuestions(solvedQuiz.getMaxScore())
                        .solvedAt(solvedQuiz.getSolvedAt())
                        .build()
        );
    }

    @Override
    @Transactional
    public Long evaluateQuiz(String viewToken, Map<String, String> formData, String username) {
        Quiz quiz = super.loadTempQuiz(viewToken);

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

        this.categoryStatisticsService.updateOnQuizCompleted(quiz.getCategoryId(), (int) correctAnswers, totalQuestions);

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
                .correctAnswers(correctAnswers)
                .totalQuestions(totalQuestions)
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