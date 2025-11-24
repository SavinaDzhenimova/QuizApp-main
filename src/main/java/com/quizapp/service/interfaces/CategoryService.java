package com.quizapp.service.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quizapp.model.dto.category.AddCategoryDTO;
import com.quizapp.model.dto.category.CategoryDTO;
import com.quizapp.model.dto.category.CategoryPageDTO;
import com.quizapp.model.dto.category.UpdateCategoryDTO;
import com.quizapp.model.entity.Result;

public interface CategoryService {

    CategoryPageDTO<CategoryDTO> getAllCategories(String categoryName, int page, int size);

    CategoryDTO getCategoryById(Long id);

    Result addCategory(AddCategoryDTO addCategoryDTO) throws JsonProcessingException;

    String getCategoryNameById(Long id);

    Result updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);
}