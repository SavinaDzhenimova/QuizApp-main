package com.quizapp.model.enums;

public enum ProblemType {
    WRONG_QUESTION("Грешен въпрос"),
    WRONG_ANSWER("Грешен отговор"),
    CATEGORY_ISSUE("Проблем с категория"),
    SPELLING_ERROR("Правописна/граматична грешка"),
    TECHNICAL("Технически проблем"),
    OTHER("Друго");

    private final String displayName;

    ProblemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}