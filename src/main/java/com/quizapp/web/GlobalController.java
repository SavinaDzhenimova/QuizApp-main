package com.quizapp.web;

import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.model.enums.ProblemType;
import com.quizapp.model.enums.QuestionSortField;
import com.quizapp.model.enums.UserSortField;
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
        CategoryPageDTO<CategoryDTO> categoryPageDTO = this.categoryService.getAllCategories("", 0, 100);
        return categoryPageDTO.getCategories();
    }

    @ModelAttribute("problemTypes")
    public ProblemType[] getProblemTypes() {
        return ProblemType.values();
    }

    @ModelAttribute("questionSortFields")
    public QuestionSortField[] getQuestionSortFields() {
        return QuestionSortField.values();
    }

    @ModelAttribute("userSortFields")
    public UserSortField[] getUserSortFields() {
        return UserSortField.values();
    }
}