package com.quizapp.model.annotations;

import com.quizapp.validation.UsernameConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = UsernameConstraintValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ValidUsername {
    String message() default "Невалидно потребителско име!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}