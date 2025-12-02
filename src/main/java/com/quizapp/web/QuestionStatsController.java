package com.quizapp.web;

import com.quizapp.model.dto.question.QuestionStatsDTO;
import com.quizapp.model.enums.QuestionSortField;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/statistics/questions")
@RequiredArgsConstructor
public class QuestionStatsController {

    private final QuestionStatisticsService questionStatisticsService;

    @GetMapping
    public ModelAndView showQuestionsStats(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                                           @RequestParam(value = "sortBy", required = false) QuestionSortField sortBy,
                                           @RequestParam(value = "questionText", required = false) String questionText) {

        ModelAndView modelAndView = new ModelAndView("questions-dashboard");

        Sort sort = Sort.unsorted();
        if (sortBy != null) {
            sort = Sort.by(sortBy.getFieldName()).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<QuestionStatsDTO> questionStatsDTOs = this.questionStatisticsService
                .getFilteredQuestionStatistics(categoryId, questionText, pageable);

        modelAndView.addObject("questionStats", questionStatsDTOs.getContent());
        modelAndView.addObject("currentPage", questionStatsDTOs.getNumber());
        modelAndView.addObject("totalPages", questionStatsDTOs.getTotalPages());
        modelAndView.addObject("totalElements", questionStatsDTOs.getTotalElements());
        modelAndView.addObject("size", questionStatsDTOs.getSize());

        modelAndView.addObject("categoryId", categoryId);
        modelAndView.addObject("sortBy", sortBy);
        modelAndView.addObject("questionText", questionText);

        if (questionStatsDTOs.getTotalElements() == 0) {
            modelAndView.addObject("warning", "Няма намерени статистики за въпроси.");
        }

        return modelAndView;
    }
}