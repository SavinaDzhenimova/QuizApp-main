package com.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionPageDTO<T> {

    private List<T> questions;

    private int totalPages;

    private long totalElements;

    private int currentPage;

    private int size;
}