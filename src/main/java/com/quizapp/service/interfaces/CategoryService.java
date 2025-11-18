package com.quizapp.service.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.dto.CategoryPageDTO;
import com.quizapp.model.dto.UpdateCategoryDTO;
import com.quizapp.model.entity.Result;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryPageDTO<CategoryDTO> getAllCategories(Pageable pageable);

    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(Long id);

    Result addCategory(AddCategoryDTO addCategoryDTO) throws JsonProcessingException;

    boolean deleteCategoryById(Long id);

    String getCategoryNameById(Long id);

    Result updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);
}