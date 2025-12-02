package com.quizapp.web;

import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.model.dto.question.QuestionStatsDTO;
import com.quizapp.model.dto.user.UserStatisticsDTO;
import com.quizapp.model.enums.QuestionSortField;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import com.quizapp.service.interfaces.QuestionStatisticsService;
import com.quizapp.service.interfaces.UserStatisticsService;
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

import java.util.List;

@Controller
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final CategoryStatisticsService categoryStatisticsService;
    private final QuestionStatisticsService questionStatisticsService;
    private final UserStatisticsService userStatisticsService;

    @GetMapping("/categories")
    public ModelAndView showCategoriesStats() {
        ModelAndView modelAndView = new ModelAndView("categories-dashboard");

        List<CategoryStatsDTO> categoryStatsDTOs = this.categoryStatisticsService.getAllCategoriesStatsForCharts();

        modelAndView.addObject("categoriesStats", categoryStatsDTOs);

        return modelAndView;
    }

    @GetMapping("/questions")
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

    @GetMapping("/users")
    public ModelAndView showUsersStats(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        ModelAndView modelAndView = new ModelAndView("users-dashboard");

        Pageable pageable = PageRequest.of(page, size);
        Page<UserStatisticsDTO> userStatisticsDTOs = this.userStatisticsService.getUserStatisticsFiltered(pageable);

        modelAndView.addObject("userStats", userStatisticsDTOs.getContent());
        modelAndView.addObject("currentPage", userStatisticsDTOs.getNumber());
        modelAndView.addObject("totalPages", userStatisticsDTOs.getTotalPages());
        modelAndView.addObject("totalElements", userStatisticsDTOs.getTotalElements());
        modelAndView.addObject("size", userStatisticsDTOs.getSize());

        return modelAndView;
    }
}
