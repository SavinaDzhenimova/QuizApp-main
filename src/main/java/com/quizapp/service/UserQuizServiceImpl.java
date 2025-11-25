package com.quizapp.service;

import com.quizapp.exception.*;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.SolvedQuizRepository;
import com.quizapp.service.interfaces.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserQuizServiceImpl implements UserQuizService {

    private final SolvedQuizRepository solvedQuizRepository;
    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final UserService userService;
    private final UserStatisticsService userStatisticsService;
    private final Map<String, Quiz> tempQuizzes = new ConcurrentHashMap<>();

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
    public Quiz createQuiz(Long categoryId, int numberOfQuestions) {
        List<QuestionApiDTO> questionApiDTOs = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));
        if (questionApiDTOs.isEmpty()) {
            throw new NoQuestionsFoundException("Няма налични въпроси в тази категория.");
        }

        if (questionApiDTOs.size() < numberOfQuestions) {
            throw new NotEnoughQuestionsException("Броят на въпросите налични в тази категория не е достатъчен, за да започнете куиз.");
        }

        String categoryName = this.categoryService.getCategoryNameById(categoryId)
                .describeConstable()
                .orElseThrow(() -> new CategoryNotFoundException("Категорията не е намерена."));

        Collections.shuffle(questionApiDTOs);
        List<QuestionDTO> questionDTOs = questionApiDTOs.stream()
                .limit(numberOfQuestions)
                .map(this.questionService::mapQuestionApiToDTO)
                .toList();

        String viewToken = UUID.randomUUID().toString();
        Quiz quiz = Quiz.builder()
                .viewToken(viewToken)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .questions(questionDTOs)
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .build();

        this.tempQuizzes.put(viewToken, quiz);
        return quiz;
    }

    @Override
    public Quiz getSolvedQuizByViewToken(String viewToken) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quiz;
    }

    private Map<Long, String> mapUserAnswers(Map<String, String> formData) {
        Map<Long, String> userAnswers = new HashMap<>();

        formData.forEach((key, value) -> {
            if (key.startsWith("answers[")) {
                Long questionId = Long.valueOf(key.replaceAll("[^0-9]", ""));
                userAnswers.put(questionId, value);
            }
        });

        return userAnswers;
    }

    @Override
    @Transactional
    public Long evaluateQuiz(String viewToken, Map<String, String> formData, String username) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        User user = this.userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Потребителят не е намерен."));

        Map<Long, String> userAnswers = this.mapUserAnswers(formData);

        this.tempQuizzes.remove(viewToken);

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

    private Long getCorrectAnswers(Quiz quiz, Map<Long, String> userAnswers) {
        return quiz.getQuestions().stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();
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