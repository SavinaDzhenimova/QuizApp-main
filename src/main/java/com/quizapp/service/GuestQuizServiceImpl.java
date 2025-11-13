package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.GuestQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GuestQuizServiceImpl implements GuestQuizService {

    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final Map<Long, Quiz> tempQuizzes = new ConcurrentHashMap<>();
    private static long TEMP_ID_COUNTER = 1L;

    @Override
    public Quiz getSolvedQuizById(Long id) {
        return this.tempQuizzes.get(id);
    }

    @Override
    public Quiz createQuiz(Long categoryId, int numberOfQuestions) {
        List<Question> allQuestions = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));

        Collections.shuffle(allQuestions);

        List<QuestionDTO> questionDTOs = allQuestions.stream()
                .limit(numberOfQuestions)
                .map(this.questionService::questionToDTO)
                .toList();

        Long tempId = TEMP_ID_COUNTER++;

        Quiz quiz = Quiz.builder()
                .id(tempId)
                .categoryId(categoryId)
                .categoryName(this.categoryService.getCategoryNameById(categoryId))
                .questions(questionDTOs)
                .build();

        this.tempQuizzes.put(tempId, quiz);
        return quiz;
    }

    @Override
    public QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> formData) {
        Quiz quiz = this.tempQuizzes.get(quizId);

        if (quiz == null) return null;

        Map<Long, String> userAnswers = this.parseUserAnswers(formData);

        int totalQuestions = quiz.getQuestions().size();
        long correctAnswers = this.getCorrectAnswers(quiz, userAnswers);

        this.tempQuizzes.remove(quizId);

        return QuizResultDTO.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers((int) correctAnswers)
                .scorePercent((double) correctAnswers / totalQuestions * 100)
                .build();
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