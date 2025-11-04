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
        // Извикваме REST API-то чрез RestClient
        List<Question> allQuestions = Arrays.asList(
                restClient.get()
                        .uri("/api/questions/category/{id}", categoryId)
                        .retrieve()
                        .body(Question[].class)
        );

        if (allQuestions.isEmpty()) {
            throw new RuntimeException("Няма въпроси за тази категория.");
        }

        // Разбъркваме и избираме случайни N
        Collections.shuffle(allQuestions);
        List<Long> selectedIds = allQuestions.stream()
                .limit(numberOfQuestions)
                .map(Question::getId)
                .collect(Collectors.toList());

        // Създаваме и записваме Quiz в локалната база
        Quiz quiz = new Quiz();
        quiz.setCategoryId(categoryId);
        quiz.setQuestionsIds(selectedIds);

        return this.quizRepository.saveAndFlush(quiz);
    }
}