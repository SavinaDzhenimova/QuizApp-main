package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.GuestQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GuestQuizServiceImpl implements GuestQuizService {

    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final Map<String, Quiz> tempQuizzes = new ConcurrentHashMap<>();
    private final Map<String, SolvedQuizDTO> guestQuizResults = new ConcurrentHashMap<>();

    @Override
    public Quiz getSolvedQuizByViewToken(String viewToken) {
        return this.tempQuizzes.get(viewToken);
    }

    @Override
    public Quiz createQuiz(Long categoryId, int numberOfQuestions) {
        List<QuestionApiDTO> questionApiDTOs = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));

        Collections.shuffle(questionApiDTOs);

        List<QuestionDTO> questionDTOs = questionApiDTOs.stream()
                .limit(numberOfQuestions)
                .map(this.questionService::mapQuestionApiToDTO)
                .toList();

        String viewToken = UUID.randomUUID().toString();
        String categoryName = this.categoryService.getCategoryNameById(categoryId);

        Quiz quiz = Quiz.builder()
                .viewToken(viewToken)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .questions(questionDTOs)
                .build();

        this.tempQuizzes.put(viewToken, quiz);
        return quiz;
    }

    @Override
    public QuizResultDTO evaluateQuiz(String viewToken, Map<String, String> formData) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) return null;

        Map<Long, String> userAnswers = this.parseUserAnswers(formData);

        int totalQuestions = quiz.getQuestions().size();
        long correctAnswers = this.getCorrectAnswers(quiz, userAnswers);

        this.tempQuizzes.remove(viewToken);

        String token = this.saveQuizResult((int) correctAnswers, totalQuestions, quiz, userAnswers);

        return QuizResultDTO.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers((int) correctAnswers)
                .scorePercent((double) correctAnswers / totalQuestions * 100)
                .token(token)
                .build();
    }

    private String saveQuizResult(int correctAnswers, int totalQuestions, Quiz quiz, Map<Long, String> userAnswers) {
        String token = UUID.randomUUID().toString();

        SolvedQuizDTO solvedQuizDTO = SolvedQuizDTO.builder()
                .score(correctAnswers)
                .maxScore(totalQuestions)
                .categoryName(quiz.getCategoryName())
                .solvedAt(LocalDateTime.now())
                .questions(quiz.getQuestions())
                .userAnswers(userAnswers)
                .build();

        this.guestQuizResults.put(token, solvedQuizDTO);

        return token;
    }

    @Override
    public SolvedQuizDTO showSolvedQuizResult(String token) {
        return this.guestQuizResults.get(token);
    }

    private Long getCorrectAnswers(Quiz quiz, Map<Long, String> userAnswers) {
        return quiz.getQuestions().stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();
    }

    private Map<Long, String> parseUserAnswers(Map<String, String> formData) {
        Map<Long, String> result = new HashMap<>();

        formData.forEach((key, val) -> {
            if (key.startsWith("answers[")) {
                Long id = Long.parseLong(key.replaceAll("[^0-9]", ""));
                result.put(id, val);
            }
        });

        return result;
    }
}