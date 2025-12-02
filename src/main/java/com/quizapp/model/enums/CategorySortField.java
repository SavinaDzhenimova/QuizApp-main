package com.quizapp.model.enums;

public enum CategorySortField {
    TOTAL_STARTED_QUIZZES("totalStartedQuizzes", "Започнати куизове"),
    TOTAL_COMPLETED_QUIZZES("totalCompletedQuizzes", "Решени куизове"),
    TOTAL_CORRECT_ANSWERS("totalCorrectAnswers", "Верни отговори"),
    TOTAL_QUESTIONS("totalQuestions", "Решени въпроси"),
    AVERAGE_ACCURACY("averageAccuracy", "Средна точност"),
    COMPLETION_RATE("completionRate", "Завършеност");

    private final String fieldName;
    private final String displayName;

    CategorySortField(String fieldName, String displayName) {
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
