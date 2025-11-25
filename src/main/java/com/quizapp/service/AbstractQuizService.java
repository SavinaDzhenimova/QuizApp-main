package com.quizapp.service;

import com.quizapp.exception.CategoryNotFoundException;
import com.quizapp.exception.NoQuestionsFoundException;
import com.quizapp.exception.NotEnoughQuestionsException;
import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public abstract class AbstractQuizService {

    protected final QuestionService questionService;
    protected final CategoryService categoryService;
    protected final Map<String, Quiz> tempQuizzes = new ConcurrentHashMap<>();

    protected Quiz getQuizFromTemp(String viewToken) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        return quiz;
    }

    protected Quiz createQuiz(Long categoryId, int numberOfQuestions) {
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

    protected Quiz loadAndRemoveTempQuiz(String viewToken) {
        Quiz quiz = this.tempQuizzes.get(viewToken);

        if (quiz == null) {
            throw new QuizNotFoundException("Куизът не е намерен.");
        }

        this.tempQuizzes.remove(viewToken);

        return quiz;
    }

    protected Long getCorrectAnswers(Quiz quiz, Map<Long, String> userAnswers) {
        return quiz.getQuestions().stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();
    }

    protected Map<Long, String> mapUserAnswers(Map<String, String> formData) {
        Map<Long, String> userAnswers = new HashMap<>();

        formData.forEach((key, value) -> {
            if (key.startsWith("answers[")) {
                Long questionId = Long.valueOf(key.replaceAll("[^0-9]", ""));
                userAnswers.put(questionId, value);
            }
        });

        return userAnswers;
    }
}