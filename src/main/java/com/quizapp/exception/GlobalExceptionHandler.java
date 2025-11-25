package com.quizapp.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPasswordResetToken.class)
    public ModelAndView handleInvalidPasswordResetToken(InvalidPasswordResetToken ex) {
        ModelAndView modelAndView = new ModelAndView("error/invalid-token");

        modelAndView.addObject("message", ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(NotEnoughQuestionsException.class)
    public ModelAndView handleNotEnoughQuestions(NotEnoughQuestionsException ex) {
        ModelAndView modelAndView = new ModelAndView("error/not-enough-questions");

        modelAndView.addObject("message", ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error/object-not-found");

        modelAndView.addObject("message", ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ModelAndView handleCategoryNotFound(CategoryNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error/object-not-found");

        modelAndView.addObject("message", ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(QuestionsNotFoundException.class)
    public ModelAndView handleQuestionsNotFound(QuestionsNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error/object-not-found");

        modelAndView.addObject("message", ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        return new ModelAndView("error/403");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView handleResourceNotFound(NoSuchElementException ex) {
        return new ModelAndView("error/404");
    }
}