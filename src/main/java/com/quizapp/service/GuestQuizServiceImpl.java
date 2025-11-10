package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.Quiz;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GuestQuizServiceImpl implements QuizService {

    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final Map<Long, Quiz> tempQuizzes = new ConcurrentHashMap<>();
    private static long TEMP_ID_COUNTER = 1L;

    @Override
    public QuizDTO getQuizById(Long id) {
        Quiz quiz = this.tempQuizzes.get(id);
        if (quiz == null) return null;

        List<QuestionDTO> questions = quiz.getQuestionsIds().stream()
                .map(this.questionService::getQuestionById)
                .toList();

        return QuizDTO.builder()
                .id(id)
                .categoryId(quiz.getCategoryId())
                .categoryName(this.categoryService.getCategoryNameById(quiz.getCategoryId()))
                .questions(questions)
                .build();
    }

    @Override
    public Quiz createQuiz(Long categoryId, int numberOfQuestions) {
        List<Question> allQuestions = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));
        Collections.shuffle(allQuestions);

        List<Long> questionIds = allQuestions.stream()
                .limit(numberOfQuestions)
                .map(Question::getId)
                .toList();

        Long tempId = TEMP_ID_COUNTER++;

        Quiz quiz = Quiz.builder()
                .id(tempId)
                .categoryId(categoryId)
                .questionsIds(questionIds)
                .build();

        this.tempQuizzes.put(tempId, quiz);
        return quiz;
    }

    @Override
    public QuizDTO mapQuizToDTO(Long quizId, Long categoryId) {
        return null;
    }

    @Override
    public QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> formData) {
        Quiz quiz = this.tempQuizzes.get(quizId);

        if (quiz == null) return null;

        Map<Long, String> userAnswers = this.parseAnswers(formData);

        List<Question> questions = quiz.getQuestionsIds().stream()
                .map(this.questionService::makeGetRequest)
                .toList();

        long correctAnswers = questions.stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();

        this.tempQuizzes.remove(quizId);

        return QuizResultDTO.builder()
                .totalQuestions(questions.size())
                .correctAnswers((int) correctAnswers)
                .scorePercent((double) correctAnswers / questions.size() * 100)
                .build();
    }

    private Map<Long, String> parseAnswers(Map<String, String> formData) {
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