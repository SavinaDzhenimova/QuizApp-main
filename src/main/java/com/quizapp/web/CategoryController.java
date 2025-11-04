package com.quizapp.web;

import com.quizapp.model.dto.CategoryDTO;
import com.quizapp.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
