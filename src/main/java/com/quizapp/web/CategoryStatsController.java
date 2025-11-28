package com.quizapp.web;

import com.quizapp.model.dto.category.CategoryStatsDTO;
import com.quizapp.service.interfaces.CategoryStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/categories/dashboard")
@RequiredArgsConstructor
public class CategoryStatsController {

    private final CategoryStatisticsService categoryStatsService;

    @GetMapping
    public ModelAndView showCategoriesStats() {
        ModelAndView modelAndView = new ModelAndView("categories-dashboard");

        List<CategoryStatsDTO> categoryStatsDTOs = this.categoryStatsService.getAllCategoriesStatsForCharts();

        modelAndView.addObject("categoriesStats", categoryStatsDTOs);

        return modelAndView;
    }
}