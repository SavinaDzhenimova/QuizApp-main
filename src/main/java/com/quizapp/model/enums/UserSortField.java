package com.quizapp.model.enums;

public enum UserSortField {
    TOTAL_QUIZZES("totalSolvedQuizzes", "Решени куизове"),
    TOTAL_CORRECT_ANSWERS("totalCorrectAnswers", "Верни отговори"),
    MAX_SCORE("maxScore", "Решени въпроси"),
    AVERAGE_SCORE("averageScore", "Среден резултат"),
    LAST_SOLVED_AT("lastSolvedAt", "Последно решавал");

    private final String fieldName;
    private final String displayName;

    UserSortField(String fieldName, String displayName) {
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