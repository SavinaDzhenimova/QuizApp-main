package com.quizapp.repository.spec;

import com.quizapp.model.entity.QuestionStatistics;
import org.springframework.data.jpa.domain.Specification;

public class QuestionStatisticsSpecifications {

    public static Specification<QuestionStatistics> hasQuestionText(String questionText) {
        return (root, query, cb) ->
                questionText == null || questionText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("questionText")), "%" + questionText.trim().toLowerCase() + "%");
    }

    public static Specification<QuestionStatistics> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null
                        ? null
                        : cb.equal(root.get("categoryId"), categoryId);
    }
}