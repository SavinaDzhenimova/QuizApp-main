package com.quizapp.service;

import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.Quiz;
import com.quizapp.repository.QuizRepository;
import com.quizapp.service.interfaces.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final RestClient restClient;

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
}