package com.quizapp.service;

import com.quizapp.exception.CategoryNotFoundException;
import com.quizapp.exception.NotEnoughQuestionsException;
import com.quizapp.exception.NoQuestionsFoundException;
import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
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
    private final Map<String, QuizDTO> guestQuizResults = new ConcurrentHashMap<>();

    @Override
    public Quiz getSolvedQuizByViewToken(String viewToken) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quiz;
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

        Collections.shuffle(questionApiDTOs);
        List<QuestionDTO> questionDTOs = questionApiDTOs.stream()
                .limit(numberOfQuestions)
                .map(this.questionService::mapQuestionApiToDTO)
                .toList();

        String viewToken = UUID.randomUUID().toString();
        String categoryName = this.categoryService.getCategoryNameById(categoryId)
                .describeConstable()
                .orElseThrow(() -> new CategoryNotFoundException("Категорията не е намерена."));

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
    public void evaluateQuiz(String viewToken, Map<String, String> formData) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        Map<Long, String> userAnswers = this.parseUserAnswers(formData);

        this.tempQuizzes.remove(viewToken);

        this.saveQuizResult(quiz, userAnswers);
    }

    @Override
    public QuizResultDTO getQuizResult(String viewToken) {
        QuizDTO quizDTO = this.guestQuizResults.get(viewToken);

        if (quizDTO == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return QuizResultDTO.builder()
                .viewToken(viewToken)
                .correctAnswers(quizDTO.getCorrectAnswers())
                .totalQuestions(quizDTO.getTotalQuestions())
                .scorePercent(quizDTO.getScorePercent())
                .build();
    }

    private void saveQuizResult(Quiz quiz, Map<Long, String> userAnswers) {

        int totalQuestions = quiz.getQuestions().size();
        long correctAnswers = this.getCorrectAnswers(quiz, userAnswers);
        double scorePercent = ((double) correctAnswers / totalQuestions) * 100;

        QuizDTO quizDTO = QuizDTO.builder()
                .viewToken(quiz.getViewToken())
                .correctAnswers((int) correctAnswers)
                .totalQuestions(totalQuestions)
                .scorePercent(scorePercent)
                .categoryName(quiz.getCategoryName())
                .solvedAt(LocalDateTime.now())
                .expireAt(quiz.getExpireAt())
                .questions(quiz.getQuestions())
                .userAnswers(userAnswers)
                .build();

        this.guestQuizResults.put(quiz.getViewToken(), quizDTO);
    }

    @Override
    public QuizDTO showQuizResult(String viewToken) {
        QuizDTO quizDTO = this.guestQuizResults.get(viewToken);

        if (quizDTO == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quizDTO;
    }

    @Override
    public void deleteExpiredGuestQuizzes(LocalDateTime dateTime) {
        this.guestQuizResults.entrySet()
                .removeIf(entry -> entry.getValue().getExpireAt().isBefore(dateTime));
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