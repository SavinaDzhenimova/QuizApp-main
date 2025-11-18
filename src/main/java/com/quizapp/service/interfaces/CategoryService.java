package com.quizapp.service.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.dto.CategoryPageDTO;
import com.quizapp.model.dto.UpdateCategoryDTO;
import com.quizapp.model.entity.Result;

import java.util.List;

public interface CategoryService {

    CategoryPageDTO<CategoryDTO> getAllCategories(int page, int size);

    CategoryDTO getCategoryById(Long id);

    Result addCategory(AddCategoryDTO addCategoryDTO) throws JsonProcessingException;

    boolean deleteCategoryById(Long id);

    String getCategoryNameById(Long id);

    Result updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);
}