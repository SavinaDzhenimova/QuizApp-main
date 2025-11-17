package com.quizapp.model.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ApiResponse {
    SUCCESS(HttpStatus.OK),
    CREATED(HttpStatus.CREATED),
    DELETED(HttpStatus.NO_CONTENT),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ApiResponse(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiResponse fromStatus(HttpStatusCode statusCode) {
        for (ApiResponse r : values()) {
            if (r.status.value() == statusCode.value()) return r;
        }
        return ERROR;
    }
}