package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    CategoryDTO getCategoryById(Long id);

    List<CategoryDTO> getAllCategories();

    Object addCategory(AddCategoryDTO addCategoryDTO);

    boolean deleteCategoryById(Long id);

    String getCategoryNameById(Long id);
}
