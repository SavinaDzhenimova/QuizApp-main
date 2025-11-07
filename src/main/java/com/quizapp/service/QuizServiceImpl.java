package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.Quiz;
import com.quizapp.repository.QuizRepository;
import com.quizapp.service.interfaces.CategoryService;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final RestClient restClient;

    @Override
    public QuizDTO getQuizById(Long id) {
        Optional<Quiz> optionalQuiz = this.quizRepository.findById(id);

        if (optionalQuiz.isEmpty()) {
            return null;
        }

        Quiz quiz = optionalQuiz.get();

        List<QuestionDTO> questions = quiz.getQuestionsIds().stream()
                .map(questionId -> restClient.get()
                        .uri("/api/questions/{id}", questionId)
                        .retrieve()
                        .body(Question.class))
                .map(question -> QuestionDTO.builder()
                        .id(question.getId())
                        .questionText(question.getQuestionText())
                        .correctAnswer(question.getCorrectAnswer())
                        .options(question.getOptions())
                        .build())
                .toList();

        return QuizDTO.builder()
                .id(quiz.getId())
                .categoryId(quiz.getCategoryId())
                .questions(questions)
                .build();
    }

    @Override
    public Quiz createQuiz(Long categoryId, int numberOfQuestions) {
        List<Question> allQuestions = Arrays.asList(
                restClient.get()
                        .uri("/api/questions/category/{id}", categoryId)
                        .retrieve()
                        .body(Question[].class)
        );

        if (allQuestions.isEmpty()) {
            return null;
        }

        Collections.shuffle(allQuestions);
        List<Long> selectedIds = allQuestions.stream()
                .limit(numberOfQuestions)
                .map(Question::getId)
                .collect(Collectors.toList());

        Quiz quiz = Quiz.builder()
                .categoryId(categoryId)
                .questionsIds(selectedIds)
                .build();

        return this.quizRepository.saveAndFlush(quiz);
    }

    @Override
    public QuizDTO mapQuizToDTO(Quiz quiz, Long categoryId) {
        List<QuestionDTO> questionDTOs = quiz.getQuestionsIds()
                .stream()
                .map(this.questionService::getQuestionById)
                .toList();

        String categoryName = this.categoryService.getCategoryNameById(categoryId);

        return QuizDTO.builder()
                .id(quiz.getId())
                .categoryId(quiz.getCategoryId())
                .categoryName(categoryName)
                .questions(questionDTOs)
                .build();
    }

    @Override
    public boolean deleteQuizById(Long id) {
        if (!this.quizRepository.existsById(id)) {
            return false;
        }

        this.quizRepository.deleteById(id);
        return true;
    }
}