package com.quizapp.web;

import com.quizapp.model.dto.AddCategoryDTO;
import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.model.entity.Category;
import com.quizapp.service.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categoryDTOs = this.categoryService.getAllCategories();
        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        CategoryDTO categoryDTO = this.categoryService.getCategoryById(id);

        if (categoryDTO == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Категория с ID " + id + " не е намерена."));
        }

        return ResponseEntity.ok(categoryDTO);
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody @Valid AddCategoryDTO addCategoryDTO) {
        Category savedCategory = this.categoryService.addCategory(addCategoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        boolean isDeleted = this.categoryService.deleteCategoryById(id);

        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Категория с ID " + id + " не е намерена, за да бъде премахната."));
        }

        return ResponseEntity.noContent().build();
    }
}