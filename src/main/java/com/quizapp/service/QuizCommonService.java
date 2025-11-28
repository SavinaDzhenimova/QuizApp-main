package com.quizapp.service;

import com.quizapp.exception.CategoryNotFoundException;
import com.quizapp.exception.NoQuestionsFoundException;
import com.quizapp.exception.NotEnoughQuestionsException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.utils.AbstractQuizHelper;
import com.quizapp.service.utils.TempQuizStorage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizCommonService extends AbstractQuizHelper {

    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final CategoryStatisticsService categoryStatisticsService;

    public QuizCommonService(TempQuizStorage tempQuizStorage, QuestionService questionService,
                             CategoryService categoryService, CategoryStatisticsService categoryStatisticsService) {
        super(tempQuizStorage);
        this.questionService = questionService;
        this.categoryService = categoryService;
        this.categoryStatisticsService = categoryStatisticsService;
    }

    public Quiz getQuizFromTemp(String viewToken) {
        return super.loadTempQuiz(viewToken);
    }

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

        this.categoryStatisticsService.increaseStartedQuizzes(categoryId);

        super.putTempQuiz(viewToken, quiz);
        return quiz;
    }
}