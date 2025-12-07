package com.quizapp.model.annotations;

import com.quizapp.validation.AllQuestionsAnsweredValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = AllQuestionsAnsweredValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface AllQuestionsAnswered {
    String message() default "Трябва да отговорите на всички въпроси!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}