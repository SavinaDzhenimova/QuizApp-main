package com.quizapp.service;

import com.quizapp.repository.QuestionStatisticsRepository;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionStatisticsServiceImpl implements QuestionStatisticsService {

    private final QuestionStatisticsRepository questionStatisticsRepository;

    
}