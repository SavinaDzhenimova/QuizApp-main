package com.quizapp.web.view;

import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ModelAndView showCategoriesPage() {
        ModelAndView modelAndView = new ModelAndView("categories");

        List<CategoryDTO> allCategories = this.categoryService.getAllCategories();

        modelAndView.addObject("categories", allCategories);

        return modelAndView;
    }
}