package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.SolvedQuizRepository;
import com.quizapp.service.interfaces.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQuizServiceImpl implements UserQuizService {

    private final SolvedQuizRepository solvedQuizRepository;
    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final UserStatisticsService userStatisticsService;

    @Override
    public SolvedQuizDTO getSolvedQuizById(Long id) {
        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(id);

        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }

        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

        List<QuestionDTO> questions = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::makeGetRequest)
                .map(this.questionService::questionToDTO)
                .toList();

        return SolvedQuizDTO.builder()
                .id(solvedQuiz.getId())
                .categoryId(solvedQuiz.getCategoryId())
                .categoryName(this.categoryService.getCategoryNameById(solvedQuiz.getCategoryId()))
                .questions(questions)
                .build();
    }

    @Override
    public SolvedQuiz createQuiz(Long categoryId, int numberOfQuestions, String username) {
        Optional<User> optionalUser = this.userService.getUserByUsername(username);

        if (optionalUser.isEmpty()) {
            return null;
        }

        List<Question> allQuestions = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));

        if (allQuestions.isEmpty()) {
            return null;
        }

        Collections.shuffle(allQuestions);
        List<Long> selectedIds = allQuestions.stream()
                .limit(numberOfQuestions)
                .map(Question::getId)
                .collect(Collectors.toList());

        SolvedQuiz solvedQuiz = SolvedQuiz.builder()
                .user(optionalUser.get())
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
    public QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> formData, String username) {
        Optional<User> optionalUser = this.userService.getUserByUsername(username);
        if (optionalUser.isEmpty()) {
            return null;
        }
        User user = optionalUser.get();

        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(quizId);
        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }
        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

        Map<Long, String> userAnswers = this.mapUserAnswers(formData);

        List<Question> questions = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::makeGetRequest)
                .toList();

        long correctAnswers = questions.stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();

        int totalQuestions = questions.size();

        double scorePercent = ((double) correctAnswers / totalQuestions) * 100;

        solvedQuiz.setSolvedAt(LocalDateTime.now());
        solvedQuiz.setScore((int) correctAnswers);
        solvedQuiz.setMaxScore(totalQuestions);
        this.solvedQuizRepository.saveAndFlush(solvedQuiz);

        UserStatistics userStatistics = this.userStatisticsService
                .updateUserStatistics(user.getUserStatistics(), correctAnswers, totalQuestions);

        user.setUserStatistics(userStatistics);
        user.getSolvedQuizzes().add(solvedQuiz);
        this.userService.saveAndFlushUser(user);

        return QuizResultDTO.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers((int) correctAnswers)
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