package com.quizapp.model.enums;

public enum QuestionSortField {
    ACCURACY("accuracy", "Точност"),
    DIFFICULTY("difficulty", "Трудност"),
    COMPLETION_RATE("completionRate", "Завършеност"),
    ATTEMPTS("attempts", "Опити"),
    CORRECT_ANSWERS("correctAnswers", "Верни отговори"),
    WRONG_ANSWERS("wrongAnswers", "Грешни отговори");

    private final String fieldName;
    private final String displayName;

    QuestionSortField(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }
}