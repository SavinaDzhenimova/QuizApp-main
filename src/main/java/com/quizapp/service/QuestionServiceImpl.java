package com.quizapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<QuestionDTO> getAllQuestions() {
        Question[] questions = this.makeGetRequestAll();

        return Arrays.stream(questions)
                .map(this::questionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionDTO getQuestionById(Long id) {
        try {
            Question question = this.makeGetRequest(id);

            return this.questionToDTO(question);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public QuestionDTO questionToDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .correctAnswer(question.getCorrectAnswer())
                .options(question.getOptions())
                .build();
    }

    @Override
    public Object addQuestion(AddQuestionDTO addQuestionDTO) {
        try {
            return this.makePostRequest(addQuestionDTO);

        } catch (HttpClientErrorException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();

            try {
                Map<String, String> errors = objectMapper.readValue(responseBody, Map.class);
                return Map.of("errors", errors);
            } catch (Exception ex) {
                return Map.of("errors", Map.of("general", "Нещо се обърка при обработката на грешката"));
            }
        }
    }

    @Override
    public boolean deleteQuestionById(Long id) {
        try {
            this.makeDeleteRequest(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public Question makeGetRequest(Long id) {
        return this.restClient.get()
                .uri("/api/questions/{id}", id)
                .retrieve()
                .body(Question.class);
    }

    @Override
    public Question[] makeGetRequestAll() {
        return this.restClient.get()
                .uri("/api/questions")
                .retrieve()
                .body(Question[].class);
    }

    @Override
    public Question makePostRequest(AddQuestionDTO addQuestionDTO) {
        return this.restClient.post()
                .uri("/api/questions")
                .body(addQuestionDTO)
                .retrieve()
                .body(Question.class);
    }

    @Override
    public void makeDeleteRequest(Long id) {
        this.restClient.delete()
                .uri("/api/questions/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Question[] makeGetRequestByCategoryId(Long categoryId) {
        return this.restClient.get()
                .uri("/api/questions/category/{id}", categoryId)
                .retrieve()
                .body(Question[].class);
    }
}