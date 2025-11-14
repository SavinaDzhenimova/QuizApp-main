package com.quizapp.web.view;

import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {

    private final CategoryService categoryService;

    @ModelAttribute("categories")
    public List<CategoryDTO> getCategoriesForSelectElement() {
        return this.categoryService.getAllCategories();
    }
}