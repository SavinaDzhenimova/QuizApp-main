package com.quizapp.validation;

import com.quizapp.model.annotations.AllQuestionsAnswered;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class AllQuestionsAnsweredValidator implements ConstraintValidator<AllQuestionsAnswered, Map<Long, String>> {

    @Override
    public void initialize(AllQuestionsAnswered constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Map<Long, String> answers, ConstraintValidatorContext context) {
        if (answers == null || answers.isEmpty()) return false;

        return answers.values().stream().allMatch(answer -> answer != null && !answer.isBlank());
    }
}