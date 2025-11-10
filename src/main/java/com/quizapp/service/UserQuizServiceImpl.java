package com.quizapp.service;

import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuizResultDTO;
import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.repository.SolvedQuizRepository;
import com.quizapp.service.interfaces.QuestionService;
import com.quizapp.service.interfaces.UserQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQuizServiceImpl implements UserQuizService {

    private final SolvedQuizRepository solvedQuizRepository;
    private final QuestionService questionService;

    @Override
    public SolvedQuizDTO getSolvedQuizById(Long id) {
        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(id);

        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }

        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

        List<QuestionDTO> questions = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::makeGetRequest)
                .map(this.questionService::questionToDTO)
                .toList();

        return SolvedQuizDTO.builder()
                .categoryId(solvedQuiz.getCategoryId())
                .questions(questions)
                .build();
    }

    @Override
    public SolvedQuiz createQuiz(Long categoryId, int numberOfQuestions) {
        List<Question> allQuestions = Arrays.asList(this.questionService.makeGetRequestByCategoryId(categoryId));

        if (allQuestions.isEmpty()) {
            return null;
        }

        Collections.shuffle(allQuestions);
        List<Long> selectedIds = allQuestions.stream()
                .limit(numberOfQuestions)
                .map(Question::getId)
                .collect(Collectors.toList());

        SolvedQuiz solvedQuiz = SolvedQuiz.builder()
                .categoryId(categoryId)
                .questionIds(selectedIds)
                .build();

        return this.solvedQuizRepository.saveAndFlush(solvedQuiz);
    }

    private Map<Long, String> mapUserAnswers(Map<String, String> formData) {
        Map<Long, String> userAnswers = new HashMap<>();

        formData.forEach((key, value) -> {
            if (key.startsWith("answers[")) {
                Long questionId = Long.valueOf(key.replaceAll("[^0-9]", ""));
                userAnswers.put(questionId, value);
            }
        });

        return userAnswers;
    }

    @Override
    public QuizResultDTO evaluateQuiz(Long quizId, Map<String, String> formData) {
        Optional<SolvedQuiz> optionalSolvedQuiz = this.solvedQuizRepository.findById(quizId);

        if (optionalSolvedQuiz.isEmpty()) {
            return null;
        }

        SolvedQuiz solvedQuiz = optionalSolvedQuiz.get();

        Map<Long, String> userAnswers = this.mapUserAnswers(formData);

        List<Question> questions = solvedQuiz.getQuestionIds().stream()
                .map(this.questionService::makeGetRequest)
                .toList();

        long correctAnswers = questions.stream()
                .filter(q -> q.getCorrectAnswer().equals(userAnswers.get(q.getId())))
                .count();

        int totalQuestions = questions.size();

        for (Question q : questions) {
            String userAnswer = userAnswers.get(q.getId());

            if (userAnswer != null && userAnswer.equals(q.getCorrectAnswer())) {
                correctAnswers++;
            }
        }

        double scorePercent = ((double) correctAnswers / totalQuestions) * 100;

        return QuizResultDTO.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers((int) correctAnswers)
                .scorePercent(scorePercent)
                .build();
    }

    @Override
    public boolean deleteQuizById(Long id) {
        if (!this.solvedQuizRepository.existsById(id)) {
            return false;
        }

        this.solvedQuizRepository.deleteById(id);
        return true;
    }
}