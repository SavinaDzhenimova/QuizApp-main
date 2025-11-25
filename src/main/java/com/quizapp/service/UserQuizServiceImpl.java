package com.quizapp.service;

import com.quizapp.exception.NotEnoughQuestionsException;
import com.quizapp.exception.QuestionsNotFoundException;
import com.quizapp.exception.UserNotFoundException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQuizServiceImpl implements UserQuizService {

    private final SolvedQuizRepository solvedQuizRepository;
    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final UserService userService;
    private final UserStatisticsService userStatisticsService;

    @Override
    public QuizDTO getSolvedQuizById(Long id) {
        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(id);

        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }

        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

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
    public SolvedQuiz createQuiz(Long categoryId, int numberOfQuestions, String username) {
        User user = this.userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Потребителят не е намерен."));

        List<QuestionApiDTO> questionApiDTOs = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));
        if (questionApiDTOs.isEmpty()) {
            throw new QuestionsNotFoundException("Няма налични въпроси в тази категория.");
        }

        if (questionApiDTOs.size() < numberOfQuestions) {
            throw new NotEnoughQuestionsException("Броят на въпросите налични в тази категория не е достатъчен, за да започнете куиз.");
        }

        Collections.shuffle(questionApiDTOs);
        List<Long> selectedIds = questionApiDTOs.stream()
                .limit(numberOfQuestions)
                .map(QuestionApiDTO::getId)
                .collect(Collectors.toList());

        SolvedQuiz solvedQuiz = SolvedQuiz.builder()
                .user(user)
                .categoryId(categoryId)
                .questionIds(selectedIds)
                .build();

        return this.solvedQuizRepository.saveAndFlush(solvedQuiz);
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
    public void evaluateQuiz(Long quizId, Map<String, String> formData, String username) {
        Optional<User> optionalUser = this.userService.getUserByUsername(username);
        if (optionalUser.isEmpty()) {
            return;
        }
        User user = optionalUser.get();

        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(quizId);
        if (optionalSolvedQuiz.isEmpty()) {
            return;
        }
        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

        Map<Long, String> userAnswers = this.mapUserAnswers(formData);

        this.saveSolvedQuizResult(solvedQuiz, user, userAnswers);
    }

    private void saveSolvedQuizResult(SolvedQuiz solvedQuiz, User user, Map<Long, String> userAnswers) {
        List<QuestionApiDTO> questionApiDTOs = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::makeGetRequestById)
                .toList();

        long correctAnswers = questionApiDTOs.stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();

        int totalQuestions = questionApiDTOs.size();

        LocalDateTime solvedAt = LocalDateTime.now();
        solvedQuiz.setUserAnswers(userAnswers);
        solvedQuiz.setSolvedAt(solvedAt);
        solvedQuiz.setScore((int) correctAnswers);
        solvedQuiz.setMaxScore(totalQuestions);
        this.solvedQuizRepository.saveAndFlush(solvedQuiz);

        UserStatistics userStatistics = this.userStatisticsService
                .updateUserStatistics(user.getUserStatistics(), correctAnswers, totalQuestions, solvedAt);

        user.setUserStatistics(userStatistics);
        user.getSolvedQuizzes().add(solvedQuiz);
        this.userService.saveAndFlushUser(user);
    }

    @Override
    public QuizResultDTO getQuizResult(Long id) {
        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(id);

        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }

        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

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